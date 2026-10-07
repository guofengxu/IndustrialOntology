package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.data.FormRegionFilter;
import org.industrial.ontology.domain.form.field.FormRegionId;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormRegionFilterIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-19
 */
public record FormRegionFilterIndex(@Nonnull ImmutableSet<FormRegionFilter> filters) {

    public FormRegionFilterIndex {
        Objects.requireNonNull(filters, "Null filters");
    }

    @Nonnull
    public static FormRegionFilterIndex get(@Nonnull ImmutableSet<FormRegionFilter> filters) {
        return new FormRegionFilterIndex(filters);
    }

    public boolean hasFilter(FormRegionId formRegionId) {
        return getFilters().stream().map(FormRegionFilter::getFormRegionId).anyMatch(id -> id.equals(formRegionId));
    }

    @Nonnull
    public ImmutableSet<FormRegionFilter> getFilters() {
        return filters;
    }
}
