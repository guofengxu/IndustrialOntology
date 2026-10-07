package org.industrial.ontology.domain.form.data;

import org.industrial.ontology.domain.form.field.FormRegionId;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormRegionFilter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-16
 */
public record FormRegionFilter(@Nonnull FormRegionId formRegionId, @Nonnull PrimitiveFormControlDataMatchCriteria matchCriteria) {

    public FormRegionFilter {
        Objects.requireNonNull(formRegionId, "Null formRegionId");
        Objects.requireNonNull(matchCriteria, "Null matchCriteria");
    }

    @Nonnull
    public static FormRegionFilter get(@Nonnull FormRegionId formRegionId, @Nonnull PrimitiveFormControlDataMatchCriteria matchCriteria) {
        return new FormRegionFilter(formRegionId, matchCriteria);
    }

    @Nonnull
    public FormRegionId getFormRegionId() {
        return formRegionId;
    }

    @Nonnull
    public PrimitiveFormControlDataMatchCriteria getMatchCriteria() {
        return matchCriteria;
    }
}
