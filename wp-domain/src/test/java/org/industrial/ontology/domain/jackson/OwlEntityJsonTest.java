package org.industrial.ontology.domain.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLProperty;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins the JSON shape of OWL entities, IRIs and literals produced by {@link ObjectMapperProvider}.
 * <p>
 * Form descriptors, project settings exports and Mongo documents written by the legacy system embed this
 * shape, so it must not drift. The legacy mapper writes the entity type as its prefixed name under
 * {@code "type"} (for example {@code {"type":"owl:Class","iri":"…"}}); docs/01 §4 abbreviates this as
 * {@code "@type":"Class"}, but the bytes the old system actually wrote are the contract.
 */
class OwlEntityJsonTest {

    private static final String NS = "http://example.org/onto#";

    private final OWLDataFactory df = new OWLDataFactoryImpl();

    private final ObjectMapper mapper = new ObjectMapperProvider().get();

    static Stream<Arguments> entityTypes() {
        return Stream.of(
                Arguments.of(EntityType.CLASS, "owl:Class"),
                Arguments.of(EntityType.OBJECT_PROPERTY, "owl:ObjectProperty"),
                Arguments.of(EntityType.DATA_PROPERTY, "owl:DatatypeProperty"),
                Arguments.of(EntityType.ANNOTATION_PROPERTY, "owl:AnnotationProperty"),
                Arguments.of(EntityType.NAMED_INDIVIDUAL, "owl:NamedIndividual"),
                Arguments.of(EntityType.DATATYPE, "rdfs:Datatype"));
    }

    @ParameterizedTest
    @MethodSource("entityTypes")
    void entitySerializesAsPrefixedTypeAndIri(EntityType<?> type, String prefixedName) throws Exception {
        OWLEntity entity = df.getOWLEntity(type, IRI.create(NS + "Pizza"));

        String json = mapper.writeValueAsString(entity);

        assertThat(json).isEqualTo("{\"type\":\"" + prefixedName + "\",\"iri\":\"" + NS + "Pizza\"}");
    }

    @ParameterizedTest
    @MethodSource("entityTypes")
    void entityRoundTripsThroughOwlEntity(EntityType<?> type, String prefixedName) throws Exception {
        OWLEntity entity = df.getOWLEntity(type, IRI.create(NS + "Pizza"));

        OWLEntity read = mapper.readValue(mapper.writeValueAsString(entity), OWLEntity.class);

        assertThat(read).isEqualTo(entity);
    }

    @Test
    void specificEntityTypesDeserializeToTheirOwnClass() throws Exception {
        String json = "{\"type\":\"owl:Class\",\"iri\":\"" + NS + "Pizza\"}";

        assertThat(mapper.readValue(json, OWLClass.class)).isEqualTo(df.getOWLClass(IRI.create(NS + "Pizza")));
    }

    @Test
    void propertyDeserializerAcceptsEveryPropertyType() throws Exception {
        String json = "{\"type\":\"owl:ObjectProperty\",\"iri\":\"" + NS + "hasTopping\"}";

        OWLProperty property = mapper.readValue(json, OWLProperty.class);

        assertThat(property).isEqualTo(df.getOWLObjectProperty(IRI.create(NS + "hasTopping")));
    }

    @Test
    void mismatchedEntityTypeIsRejected() {
        String json = "{\"type\":\"owl:Class\",\"iri\":\"" + NS + "Pizza\"}";

        assertThatThrownBy(() -> mapper.readValue(json, OWLObjectProperty.class))
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void entityWithoutIriIsRejected() {
        assertThatThrownBy(() -> mapper.readValue("{\"type\":\"owl:Class\"}", OWLEntity.class))
                .isInstanceOf(JsonProcessingException.class)
                .hasMessageContaining("iri field is missing");
    }

    @Test
    void iriSerializesAsPlainString() throws Exception {
        IRI iri = IRI.create(NS + "Pizza");

        assertThat(mapper.writeValueAsString(iri)).isEqualTo("\"" + NS + "Pizza\"");
        // IRI implements CharSequence in OWL API 4; compare as objects.
        assertThat((Object) mapper.readValue("\"" + NS + "Pizza\"", IRI.class)).isEqualTo(iri);
    }

    @Test
    void languageTaggedLiteralSerializesLangAndValue() throws Exception {
        OWLLiteral literal = df.getOWLLiteral("Pizza", "en");

        String json = mapper.writeValueAsString(literal);

        assertThat(json).isEqualTo("{\"lang\":\"en\",\"value\":\"Pizza\"}");
        assertThat(mapper.readValue(json, OWLLiteral.class)).isEqualTo(literal);
    }

    @Test
    void typedLiteralSerializesDatatypeIriAndValue() throws Exception {
        OWLLiteral literal = df.getOWLLiteral(3);

        String json = mapper.writeValueAsString(literal);

        assertThat(json).isEqualTo("{\"type\":\"http://www.w3.org/2001/XMLSchema#integer\",\"value\":\"3\"}");
        assertThat(mapper.readValue(json, OWLLiteral.class)).isEqualTo(literal);
    }

    @Test
    void annotationValueDeserializesIrisAndLiterals() throws Exception {
        assertThat(mapper.readValue("\"" + NS + "Pizza\"", OWLAnnotationValue.class))
                .isEqualTo(IRI.create(NS + "Pizza"));
        assertThat(mapper.readValue("{\"lang\":\"zh\",\"value\":\"披萨\"}", OWLAnnotationValue.class))
                .isEqualTo(df.getOWLLiteral("披萨", "zh"));
    }
}
