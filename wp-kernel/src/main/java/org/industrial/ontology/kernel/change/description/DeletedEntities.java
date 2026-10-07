package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.DeletedEntities}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record DeletedEntities(@Nonnull ImmutableSet<OWLEntity> entities) implements StructuredChangeDescription {

    public DeletedEntities {
        Objects.requireNonNull(entities, "Null entities");
    }

    public static DeletedEntities get(@Nonnull ImmutableSet<OWLEntity> entities) {
        return new DeletedEntities(entities);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "DeletedEntities";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Deleted %s", getEntities());
    }

    @Nonnull
    public ImmutableSet<OWLEntity> getEntities() {
        return entities;
    }
}
