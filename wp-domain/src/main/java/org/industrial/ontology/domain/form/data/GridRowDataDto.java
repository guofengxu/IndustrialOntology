package org.industrial.ontology.domain.form.data;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridRowDataDto}.
 */
public record GridRowDataDto(@Nullable FormSubjectDto subjectInternal, @Nonnull ImmutableList<GridCellDataDto> cells) {

    public GridRowDataDto {
        Objects.requireNonNull(cells, "Null cells");
    }

    @Nonnull
    public static GridRowDataDto get(@Nullable FormSubjectDto subject, @Nonnull ImmutableList<GridCellDataDto> cellData) {
        return new GridRowDataDto(subject, cellData);
    }

    @Nonnull
    public Optional<FormSubjectDto> getSubject() {
        return Optional.ofNullable(getSubjectInternal());
    }

    /**
     * Determines whether this row contains cells that have been filtered empty
     * @return true if this row contains cells that have been filtered empty, otherwise false.
     */
    public boolean containsFilteredEmptyCells() {
        return getCells().stream().anyMatch(GridCellDataDto::isFilteredEmpty);
    }

    public GridRowData toGridRowData() {
        return GridRowData.get(getSubject().map(FormSubjectDto::toFormSubject).orElse(null), getCells().stream().map(GridCellDataDto::toGridCellData).collect(toImmutableList()));
    }

    @Nullable
    public FormSubjectDto getSubjectInternal() {
        return subjectInternal;
    }

    @Nonnull
    public ImmutableList<GridCellDataDto> getCells() {
        return cells;
    }
}
