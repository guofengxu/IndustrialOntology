package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedParent}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedParent(@Nonnull OWLEntity child, @Nonnull OWLEntity parent) implements StructuredChangeDescription {

    public AddedParent {
        Objects.requireNonNull(child, "Null child");
        Objects.requireNonNull(parent, "Null parent");
    }

    public static AddedParent get(@Nonnull OWLEntity child, @Nonnull OWLEntity parent) {
        return new AddedParent(child, parent);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedParent";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added parent %s from %s", getParent(), getChild());
    }

    @Nonnull
    public OWLEntity getChild() {
        return child;
    }

    @Nonnull
    public OWLEntity getParent() {
        return parent;
    }
}
