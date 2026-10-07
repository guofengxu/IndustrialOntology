package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.EditedLanguageTag}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record EditedLanguageTag(IRI subject, OWLAnnotationProperty property, OWLAnnotationValue value, String fromLanguageTag, String toLanguageTag) implements StructuredChangeDescription {

    public EditedLanguageTag {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(value, "Null value");
        Objects.requireNonNull(fromLanguageTag, "Null fromLanguageTag");
        Objects.requireNonNull(toLanguageTag, "Null toLanguageTag");
    }

    @Nonnull
    public static EditedLanguageTag get(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty property, @Nonnull OWLAnnotationValue value, @Nonnull String fromLanguageTag, @Nonnull String toLanguageTag) {
        return new EditedLanguageTag(subject, property, value, fromLanguageTag, toLanguageTag);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "EditedLanguageTag";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Changed language tag from %s %s on %s annotation on %s", getFromLanguageTag(), getToLanguageTag(), getProperty(), getSubject());
    }

    public IRI getSubject() {
        return subject;
    }

    public OWLAnnotationProperty getProperty() {
        return property;
    }

    public OWLAnnotationValue getValue() {
        return value;
    }

    public String getFromLanguageTag() {
        return fromLanguageTag;
    }

    public String getToLanguageTag() {
        return toLanguageTag;
    }
}
