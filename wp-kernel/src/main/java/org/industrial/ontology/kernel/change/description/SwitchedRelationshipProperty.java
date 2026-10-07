package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.SwitchedRelationshipProperty}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record SwitchedRelationshipProperty(@Nonnull OWLObject subject, @Nonnull OWLProperty fromProperty, @Nonnull OWLProperty toProperty, @Nonnull OWLObject value) implements StructuredChangeDescription {

    public SwitchedRelationshipProperty {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(fromProperty, "Null fromProperty");
        Objects.requireNonNull(toProperty, "Null toProperty");
        Objects.requireNonNull(value, "Null value");
    }

    public static SwitchedRelationshipProperty get(@Nonnull OWLObject subject, @Nonnull OWLProperty fromProperty, @Nonnull OWLProperty toProperty, @Nonnull OWLObject value) {
        return new SwitchedRelationshipProperty(subject, fromProperty, toProperty, value);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "SwitchedRelationshipProperty";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Switch relationship property from %s to %s on %s", getFromProperty(), getToProperty(), getSubject());
    }

    @Nonnull
    public OWLObject getSubject() {
        return subject;
    }

    @Nonnull
    public OWLProperty getFromProperty() {
        return fromProperty;
    }

    @Nonnull
    public OWLProperty getToProperty() {
        return toProperty;
    }

    @Nonnull
    public OWLObject getValue() {
        return value;
    }
}
