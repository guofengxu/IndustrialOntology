package org.industrial.ontology.domain.form.data;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.FilterState;
import org.industrial.ontology.domain.form.HasFilterState;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.pagination.Page;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridControlDataDto}.
 */
public record GridControlDataDto(int depth, @Nonnull GridControlDescriptor descriptor, @Nonnull Page<GridRowDataDto> rows, @Nonnull ImmutableSet<FormRegionOrdering> ordering, @Nonnull FilterState filterState) implements FormControlDataDto, HasFilterState {

    public GridControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
        Objects.requireNonNull(rows, "Null rows");
        Objects.requireNonNull(ordering, "Null ordering");
        Objects.requireNonNull(filterState, "Null filterState");
    }

    @Nonnull
    public static GridControlDataDto get(@Nonnull GridControlDescriptor descriptor, @Nonnull Page<GridRowDataDto> rows, @Nonnull ImmutableSet<FormRegionOrdering> ordering, int depth, @Nonnull FilterState filterState) {
        return new GridControlDataDto(depth, descriptor, rows, ordering, filterState);
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public GridControlData toFormControlData() {
        return GridControlData.get(getDescriptor(), getRows().transform(GridRowDataDto::toGridRowData), getOrdering());
    }

    /**
     * Determines whether this grid is empty because it has been filtered empty.
     * @return true if this grid is empty and this is due to filtering
     */
    public boolean isFilteredEmpty() {
        return getFilterState().equals(FilterState.FILTERED) && getRows().getTotalElements() == 0;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Nonnull
    public GridControlDescriptor getDescriptor() {
        return descriptor;
    }

    @Nonnull
    public Page<GridRowDataDto> getRows() {
        return rows;
    }

    @Nonnull
    public ImmutableSet<FormRegionOrdering> getOrdering() {
        return ordering;
    }

    @Override
    @Nonnull
    public FilterState getFilterState() {
        return filterState;
    }
}
