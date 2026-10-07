package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedLanguageTag}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedLanguageTag(IRI subject, OWLAnnotationProperty property, OWLAnnotationValue value, String languageTag) implements StructuredChangeDescription {

    public AddedLanguageTag {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(value, "Null value");
        Objects.requireNonNull(languageTag, "Null languageTag");
    }

    @Nonnull
    public static AddedLanguageTag get(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty property, @Nonnull OWLAnnotationValue value, @Nonnull String languageTag) {
        return new AddedLanguageTag(subject, property, value, languageTag);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedLanguageTag";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added language tag %s to %s annotation on %s", getLanguageTag(), getProperty(), getSubject());
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

    public String getLanguageTag() {
        return languageTag;
    }
}
