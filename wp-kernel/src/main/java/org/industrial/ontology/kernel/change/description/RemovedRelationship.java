package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.RemovedRelationship}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record RemovedRelationship(@Nonnull OWLObject subject, @Nonnull OWLProperty property, @Nonnull OWLObject value) implements StructuredChangeDescription {

    public RemovedRelationship {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(value, "Null value");
    }

    public static RemovedRelationship get(@Nonnull OWLObject subject, @Nonnull OWLProperty property, @Nonnull OWLObject value) {
        return new RemovedRelationship(subject, property, value);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedRelationship";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added relationship (%s %s) on %s", getProperty(), getValue(), getSubject());
    }

    @Nonnull
    public OWLObject getSubject() {
        return subject;
    }

    @Nonnull
    public OWLProperty getProperty() {
        return property;
    }

    @Nonnull
    public OWLObject getValue() {
        return value;
    }
}
