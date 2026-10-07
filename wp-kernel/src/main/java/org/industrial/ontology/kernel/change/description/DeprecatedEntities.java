package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.DeprecatedEntities}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record DeprecatedEntities(@Nonnull ImmutableSet<IRI> entities) implements StructuredChangeDescription {

    public DeprecatedEntities {
        Objects.requireNonNull(entities, "Null entities");
    }

    public static DeprecatedEntities get(@Nonnull ImmutableSet<IRI> entities) {
        return new DeprecatedEntities(entities);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "DeprecatedEntities";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Deprecated %s", getEntities());
    }

    @Nonnull
    public ImmutableSet<IRI> getEntities() {
        return entities;
    }
}
