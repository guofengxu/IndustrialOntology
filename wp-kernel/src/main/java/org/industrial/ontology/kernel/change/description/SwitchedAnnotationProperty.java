package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.SwitchedAnnotationProperty}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record SwitchedAnnotationProperty(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty fromProperty, @Nonnull OWLAnnotationProperty toProperty, @Nonnull OWLAnnotationValue value) implements StructuredChangeDescription {

    public SwitchedAnnotationProperty {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(fromProperty, "Null fromProperty");
        Objects.requireNonNull(toProperty, "Null toProperty");
        Objects.requireNonNull(value, "Null value");
    }

    @Nonnull
    public static SwitchedAnnotationProperty get(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty fromProperty, @Nonnull OWLAnnotationProperty toProperty, @Nonnull OWLAnnotationValue value) {
        return new SwitchedAnnotationProperty(subject, fromProperty, toProperty, value);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "SwitchedAnnotationProperty";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Switch annotation property on %s from %s to %s", getSubject(), getFromProperty(), getToProperty());
    }

    @Nonnull
    public IRI getSubject() {
        return subject;
    }

    @Nonnull
    public OWLAnnotationProperty getFromProperty() {
        return fromProperty;
    }

    @Nonnull
    public OWLAnnotationProperty getToProperty() {
        return toProperty;
    }

    @Nonnull
    public OWLAnnotationValue getValue() {
        return value;
    }
}
