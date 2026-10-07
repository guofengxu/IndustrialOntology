package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.field.FormRegionId;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.FormRegionOrderingDirection;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormRegionOrderingIndex}.
 */
public record FormRegionOrderingIndex(@Nonnull ImmutableSet<FormRegionOrdering> orderings) {

    public FormRegionOrderingIndex {
        Objects.requireNonNull(orderings, "Null orderings");
    }

    public static FormRegionOrderingIndex get(@Nonnull ImmutableSet<FormRegionOrdering> orderings) {
        return new FormRegionOrderingIndex(orderings);
    }

    public FormRegionOrderingDirection getOrderingDirection(@Nonnull FormRegionId formRegionId) {
        return getOrderings().stream().filter(ordering -> ordering.getRegionId().equals(formRegionId)).findFirst().map(FormRegionOrdering::getDirection).orElse(FormRegionOrderingDirection.ASC);
    }

    @Nonnull
    public ImmutableSet<FormRegionOrdering> getOrderings() {
        return orderings;
    }
}
