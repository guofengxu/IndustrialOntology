package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.EditedAnnotationValue}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record EditedAnnotationValue(IRI subject, OWLAnnotationProperty property, OWLAnnotationValue fromValue, OWLAnnotationValue toValue) implements StructuredChangeDescription {

    public EditedAnnotationValue {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(fromValue, "Null fromValue");
        Objects.requireNonNull(toValue, "Null toValue");
    }

    public static EditedAnnotationValue get(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty property, @Nonnull OWLAnnotationValue fromValue, @Nonnull OWLAnnotationValue toValue) {
        return new EditedAnnotationValue(subject, property, fromValue, toValue);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "EditedAnnotationValue";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Changed the value of %s from %s to %s on %s", getProperty(), getFromValue(), getToValue(), getSubject());
    }

    public IRI getSubject() {
        return subject;
    }

    public OWLAnnotationProperty getProperty() {
        return property;
    }

    public OWLAnnotationValue getFromValue() {
        return fromValue;
    }

    public OWLAnnotationValue getToValue() {
        return toValue;
    }
}
