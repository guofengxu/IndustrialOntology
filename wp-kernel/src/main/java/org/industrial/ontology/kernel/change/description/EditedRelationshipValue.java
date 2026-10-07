package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.EditedRelationshipValue}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record EditedRelationshipValue(@Nonnull OWLObject subject, @Nonnull OWLProperty property, @Nonnull OWLObject fromValue, @Nonnull OWLObject toValue) implements StructuredChangeDescription {

    public EditedRelationshipValue {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(fromValue, "Null fromValue");
        Objects.requireNonNull(toValue, "Null toValue");
    }

    @Nonnull
    public static EditedRelationshipValue get(@Nonnull OWLObject subject, @Nonnull OWLProperty property, @Nonnull OWLObject fromValue, @Nonnull OWLObject toValue) {
        return new EditedRelationshipValue(subject, property, fromValue, toValue);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "EditedRelationshipValue";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Changed the value of the %s relationship from %s to %s on %s", getProperty(), getFromValue(), getToValue(), getSubject());
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
    public OWLObject getFromValue() {
        return fromValue;
    }

    @Nonnull
    public OWLObject getToValue() {
        return toValue;
    }
}
