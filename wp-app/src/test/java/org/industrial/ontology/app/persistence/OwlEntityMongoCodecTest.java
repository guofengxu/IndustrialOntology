package org.industrial.ontology.app.persistence;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwlEntityMongoCodecTest {

    private static final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private static final IRI IRI_ = IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza");

    @Test
    void shouldEncodeEveryEntityTypeAsTheLegacyConverterDid() {
        for (EntityType<?> type : EntityType.values()) {
            var document = OwlEntityMongoCodec.encode(dataFactory.getOWLEntity(type, IRI_));
            assertThat(LegacyMongoSamples.canonical(document))
                    .isEqualTo("{\"type\": \"" + type.getName() + "\", \"iri\": \"" + IRI_ + "\"}");
        }
    }

    @Test
    void shouldDecodeWhatItEncodes() {
        for (EntityType<?> type : EntityType.values()) {
            OWLEntity entity = dataFactory.getOWLEntity(type, IRI_);
            assertThat(OwlEntityMongoCodec.decode(OwlEntityMongoCodec.encode(entity))).isEqualTo(entity);
        }
    }

    /**
     * The entities embedded in the legacy samples, written by the legacy {@code OWLEntityConverter}.
     */
    @Test
    void shouldReadAndRewriteTheEntitiesOfTheLegacySamples() {
        var entities = Stream.of("EntityTags", "Watches", "EntityDiscussionThreads")
                             .flatMap(collection -> LegacyMongoSamples.documents(collection).stream())
                             .map(document -> document.get("entity", Document.class))
                             .toList();
        assertThat(entities).hasSize(5);
        for (var stored : entities) {
            assertThat(OwlEntityMongoCodec.problem(stored)).isEmpty();
            assertThat(OwlEntityMongoCodec.encode(OwlEntityMongoCodec.decode(stored))).isEqualTo(stored);
            assertThat(List.copyOf(OwlEntityMongoCodec.encode(OwlEntityMongoCodec.decode(stored)).keySet()))
                    .containsExactly("type", "iri");
        }
    }

    @Test
    void shouldReportDocumentsThatAreNotStoredEntities() {
        assertThat(OwlEntityMongoCodec.problem(null)).contains("missing");
        assertThat(OwlEntityMongoCodec.problem("Pizza")).contains("not a document");
        assertThat(OwlEntityMongoCodec.problem(new Document("iri", IRI_.toString()))).contains("missing 'type'");
        // The Jackson format of an entity, which the Morphia collections never used.
        assertThat(OwlEntityMongoCodec.problem(new Document("type", "owl:Class").append("iri", IRI_.toString())))
                .contains("unknown entity type 'owl:Class'");
        assertThat(OwlEntityMongoCodec.problem(new Document("type", "Class"))).contains("missing 'iri'");
        assertThat(OwlEntityMongoCodec.problem(new Document("type", "Class").append("iri", "")))
                .contains("missing 'iri'");
        // Mongo compares embedded documents field by field, so the legacy queries would not find this one.
        assertThat(OwlEntityMongoCodec.problem(new Document("iri", IRI_.toString()).append("type", "Class")))
                .contains("fields are [iri, type], expected [type, iri]");
    }

    @Test
    void shouldRefuseToDecodeAnInvalidDocument() {
        var document = new Document("type", "owl:Class").append("iri", IRI_.toString());
        assertThatThrownBy(() -> OwlEntityMongoCodec.decode(document))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown entity type 'owl:Class'");
    }
}
