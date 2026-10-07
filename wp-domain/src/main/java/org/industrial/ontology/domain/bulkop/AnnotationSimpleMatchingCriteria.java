package org.industrial.ontology.domain.bulkop;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.bulkop.AnnotationSimpleMatchingCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26 Sep 2018
 */
public record AnnotationSimpleMatchingCriteria(@Nullable OWLAnnotationProperty prop, boolean matchValue, @Nonnull String value, boolean valueRegularExpression, boolean matchLangTag, @Nonnull String langTag) {

    public AnnotationSimpleMatchingCriteria {
        Objects.requireNonNull(value, "Null value");
        Objects.requireNonNull(langTag, "Null langTag");
    }

    public static AnnotationSimpleMatchingCriteria get(@Nullable OWLAnnotationProperty property, boolean matchValue, @Nonnull String value, boolean regEx, boolean matchLangTag, String langTag) {
        return new AnnotationSimpleMatchingCriteria(property, matchValue, value, regEx, matchLangTag, langTag);
    }

    public Optional<OWLAnnotationProperty> getProperty() {
        return Optional.ofNullable(getProp());
    }

    @Nullable
    public OWLAnnotationProperty getProp() {
        return prop;
    }

    public boolean isMatchValue() {
        return matchValue;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    public boolean isValueRegularExpression() {
        return valueRegularExpression;
    }

    public boolean isMatchLangTag() {
        return matchLangTag;
    }

    @Nonnull
    public String getLangTag() {
        return langTag;
    }
}
