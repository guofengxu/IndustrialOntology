package org.industrial.ontology.domain.form.data;

import org.industrial.ontology.domain.form.FilterState;
import org.industrial.ontology.domain.form.HasFilterState;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.pagination.Page;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridCellDataDto}.
 */
public record GridCellDataDto(@Nonnull GridColumnId columnId, @Nonnull Page<FormControlDataDto> values, @Nonnull FilterState filterState) implements HasFilterState {

    public GridCellDataDto {
        Objects.requireNonNull(columnId, "Null columnId");
        Objects.requireNonNull(values, "Null values");
        Objects.requireNonNull(filterState, "Null filterState");
    }

    public static GridCellDataDto get(@Nonnull GridColumnId columnId, @Nullable Page<FormControlDataDto> values, @Nonnull FilterState filterState) {
        return new GridCellDataDto(columnId, values, filterState);
    }

    /**
     * Determines whether this cell data is filtered empty
     * @return true if this cel data is filtered empty (all values have been filtered out)
     * otherwise false.
     */
    public boolean isFilteredEmpty() {
        return getFilterState().equals(FilterState.FILTERED) && getValues().getPageElements().isEmpty();
    }

    public GridCellData toGridCellData() {
        return GridCellData.get(getColumnId(), getValues().transform(FormControlDataDto::toFormControlData));
    }

    @Nonnull
    public GridColumnId getColumnId() {
        return columnId;
    }

    @Nonnull
    public Page<FormControlDataDto> getValues() {
        return values;
    }

    @Override
    @Nonnull
    public FilterState getFilterState() {
        return filterState;
    }
}
