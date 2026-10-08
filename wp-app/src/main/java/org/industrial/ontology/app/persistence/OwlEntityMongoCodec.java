package org.industrial.ontology.app.persistence;

import org.bson.Document;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The storage format of an {@link OWLEntity} embedded in a Mongo document, as the legacy Morphia
 * {@code OWLEntityConverter} wrote it: {@code {"type": "Class", "iri": "http://…"}}, where {@code type} is
 * {@link EntityType#getName()} ({@code Class}, {@code ObjectProperty}, {@code DataProperty},
 * {@code AnnotationProperty}, {@code NamedIndividual} or {@code Datatype}) and comes before {@code iri}. The
 * {@code EntityTags}, {@code Watches} and {@code EntityDiscussionThreads} collections embed entities this way
 * (docs/01 §5.3); the samples in {@code legacy-mongo/} pin the format.
 * <p>
 * This is not the Jackson format of an entity ({@code {"type": "owl:Class", …}}, docs/01 §4), which the collections
 * written through Jackson (forms, tag criteria, search filters) use instead. The field order matters: Mongo compares
 * embedded documents field by field, so the legacy queries {@code {"entity": {"type": …, "iri": …}}} only match when
 * both sides are written in the same order.
 * <p>
 * Spring Data uses the codec through {@link #converters()}, which {@link MongoPersistenceAutoConfiguration} registers;
 * migrate-mongo uses {@link #problem(Object)} to report documents that do not follow the format.
 */
public final class OwlEntityMongoCodec {

    public static final String TYPE = "type";

    public static final String IRI = "iri";

    private static final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private static final Map<String, Function<IRI, OWLEntity>> entityFactories = Map.of(
            EntityType.CLASS.getName(), dataFactory::getOWLClass,
            EntityType.OBJECT_PROPERTY.getName(), dataFactory::getOWLObjectProperty,
            EntityType.DATA_PROPERTY.getName(), dataFactory::getOWLDataProperty,
            EntityType.ANNOTATION_PROPERTY.getName(), dataFactory::getOWLAnnotationProperty,
            EntityType.NAMED_INDIVIDUAL.getName(), dataFactory::getOWLNamedIndividual,
            EntityType.DATATYPE.getName(), dataFactory::getOWLDatatype);

    private OwlEntityMongoCodec() {
    }

    @Nonnull
    public static Document encode(@Nonnull OWLEntity entity) {
        checkNotNull(entity);
        return new Document(TYPE, entity.getEntityType().getName())
                .append(IRI, entity.getIRI().toString());
    }

    /**
     * @throws IllegalArgumentException if the document does not follow the legacy format. The legacy converter
     *                                  returned {@code null} instead, which then failed wherever the entity was used.
     */
    @Nonnull
    public static OWLEntity decode(@Nonnull Document document) {
        checkNotNull(document);
        problem(document).ifPresent(problem -> {
            throw new IllegalArgumentException("Not a stored OWL entity (" + problem + "): " + document.toJson());
        });
        return entityFactories.get(document.getString(TYPE))
                              .apply(org.semanticweb.owlapi.model.IRI.create(document.getString(IRI)));
    }

    /**
     * Why {@code value} is not a stored entity, or empty if it is one.
     */
    @Nonnull
    public static Optional<String> problem(Object value) {
        if (!(value instanceof Document document)) {
            return Optional.of(value == null ? "missing" : "not a document");
        }
        if (!(document.get(TYPE) instanceof String typeName)) {
            return Optional.of("missing '" + TYPE + "'");
        }
        if (!entityFactories.containsKey(typeName)) {
            return Optional.of("unknown entity type '" + typeName + "'");
        }
        if (!(document.get(IRI) instanceof String iri) || iri.isEmpty()) {
            return Optional.of("missing '" + IRI + "'");
        }
        if (!List.copyOf(document.keySet()).equals(List.of(TYPE, IRI))) {
            return Optional.of("fields are " + document.keySet() + ", expected [type, iri]");
        }
        return Optional.empty();
    }

    /**
     * The Spring Data converters for {@link OWLEntity} properties and query values.
     */
    @Nonnull
    public static List<Converter<?, ?>> converters() {
        return List.of(EntityWriter.INSTANCE, EntityReader.INSTANCE);
    }

    @WritingConverter
    enum EntityWriter implements Converter<OWLEntity, Document> {

        INSTANCE;

        @Override
        public Document convert(@Nonnull OWLEntity entity) {
            return encode(entity);
        }
    }

    @ReadingConverter
    enum EntityReader implements Converter<Document, OWLEntity> {

        INSTANCE;

        @Override
        public OWLEntity convert(@Nonnull Document document) {
            return decode(document);
        }
    }
}
