package org.industrial.ontology.domain.bulkop;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.bulkop.NewAnnotationData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 2018
 */
public record NewAnnotationData(@Nullable OWLAnnotationProperty property, @Nullable String value, @Nullable String languageTag) {

    public static NewAnnotationData get(@Nonnull Optional<OWLAnnotationProperty> property, @Nonnull Optional<String> value, @Nonnull Optional<String> languageTag) {
        return new NewAnnotationData(property.orElse(null), value.orElse(null), languageTag.orElse(null));
    }

    @Nonnull
    public Optional<OWLAnnotationProperty> getProperty() {
        return Optional.ofNullable(property());
    }

    @Nonnull
    public Optional<String> getValue() {
        return Optional.ofNullable(value());
    }

    @Nonnull
    public Optional<String> getLanguageTag() {
        return Optional.ofNullable(languageTag());
    }
}
