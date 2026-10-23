package org.industrial.ontology.api.ontology;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.domain.entity.FreshEntityIri;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Parses the parameters of the ontology editing and search endpoints (docs/02 §1): IRIs, which go in the query,
 * entity types, by their OWL API names ({@code Class}, {@code ObjectProperty}, {@code DataProperty},
 * {@code AnnotationProperty}, {@code NamedIndividual}, {@code Datatype}, case ignored, with or without the
 * {@code owl:}/{@code rdfs:} prefix of the entity JSON), and pages.
 */
final class OntologyParams {

    static final String INVALID_IRI = "INVALID_IRI";

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private OntologyParams() {
    }

    /**
     * An absolute IRI.
     *
     * @throws WpException {@value #INVALID_IRI} (400) otherwise
     */
    @Nonnull
    static IRI iri(@Nullable String iri) {
        if (iri == null || iri.isBlank()) {
            throw new WpException(INVALID_IRI, 400, "An IRI is needed");
        }
        try {
            var uri = new URI(iri);
            if (!uri.isAbsolute()) {
                throw new WpException(INVALID_IRI, 400, "The IRI is not absolute: " + iri);
            }
            if (FreshEntityIri.SCHEME.equalsIgnoreCase(uri.getScheme())) {
                // Reserved for the entities that ChangeManager is to create
                throw new WpException(INVALID_IRI, 400, "The IRI scheme entity: is reserved");
            }
        } catch (URISyntaxException e) {
            throw new WpException(INVALID_IRI, 400, "Not an IRI: " + iri);
        }
        return IRI.create(iri);
    }

    @Nonnull
    static EntityType<?> entityType(@Nullable String type) {
        if (type == null || type.isBlank()) {
            throw WpException.invalidRequest("An entity type is needed");
        }
        var name = type.strip();
        var colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        for (var entityType : EntityType.values()) {
            if (entityType.getName().equalsIgnoreCase(name)) {
                return entityType;
            }
        }
        throw WpException.invalidRequest("Unknown entity type " + type);
    }

    /**
     * Comma-separated entity types; none (or a missing parameter) is the default.
     */
    @Nonnull
    static Set<EntityType<?>> entityTypes(@Nullable String types, @Nonnull Set<EntityType<?>> defaultTypes) {
        if (types == null || types.isBlank()) {
            return defaultTypes;
        }
        var parsed = new LinkedHashSet<EntityType<?>>();
        Arrays.stream(types.split(",")).filter(type -> !type.isBlank()).map(OntologyParams::entityType)
              .forEach(parsed::add);
        return parsed;
    }

    @Nonnull
    static OWLEntity entity(@Nullable String iri, @Nullable String type) {
        return DATA_FACTORY.getOWLEntity(entityType(type), iri(iri));
    }

    @Nonnull
    static OWLEntity entity(@Nullable String iri, @Nonnull EntityType<?> type) {
        return DATA_FACTORY.getOWLEntity(type, iri(iri));
    }

    /**
     * A page request; pages start at 1 and hold 1 to {@code maxSize} elements.
     */
    @Nonnull
    static PageRequest page(int page, int size, int maxSize) {
        if (page < 1 || size < 1 || size > maxSize) {
            throw WpException.invalidRequest("Pages start at 1 and hold 1 to " + maxSize + " elements");
        }
        return PageRequest.requestPageWithSize(page, size);
    }

    @Nonnull
    static <T> T required(@Nullable T value, @Nonnull String name) {
        if (value == null) {
            throw WpException.invalidRequest(name + " needs a value");
        }
        return value;
    }

    /**
     * An enum constant by name, case ignored.
     */
    @Nonnull
    static <E extends Enum<E>> E constant(@Nonnull Class<E> type, @Nullable String name, @Nonnull String parameter) {
        if (name == null) {
            throw WpException.invalidRequest(parameter + " needs a value");
        }
        try {
            return Enum.valueOf(type, name.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw WpException.invalidRequest("Unknown " + parameter + " " + name);
        }
    }
}
