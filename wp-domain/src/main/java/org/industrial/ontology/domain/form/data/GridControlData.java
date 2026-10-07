package org.industrial.ontology.domain.form.data;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.pagination.Page;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public record GridControlData(@Nonnull GridControlDescriptor descriptor, @Nonnull Page<GridRowData> rows, @Nonnull ImmutableSet<FormRegionOrdering> ordering) implements ComplexFormControlValue {

    public GridControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
        Objects.requireNonNull(rows, "Null rows");
        Objects.requireNonNull(ordering, "Null ordering");
    }

    @Nonnull
    public static GridControlData get(@Nonnull GridControlDescriptor descriptor, @Nonnull Page<GridRowData> rows, @Nonnull ImmutableSet<FormRegionOrdering> ordering) {
        return new GridControlData(descriptor, rows, ordering);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @Nonnull
    public GridControlDescriptor getDescriptor() {
        return descriptor;
    }

    @Nonnull
    public Page<GridRowData> getRows() {
        return rows;
    }

    @Nonnull
    public ImmutableSet<FormRegionOrdering> getOrdering() {
        return ordering;
    }
}
