package org.industrial.ontology.domain.form.data;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridRowData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public record GridRowData(@Nullable FormSubject subjectInternal, @Nonnull ImmutableList<GridCellData> cells) implements Comparable<GridRowData> {

    public GridRowData {
        Objects.requireNonNull(cells, "Null cells");
    }

    public static GridRowData get(@Nullable FormSubject subject, @Nonnull ImmutableList<GridCellData> cellData) {
        return new GridRowData(subject, cellData);
    }

    @Nonnull
    public Optional<FormSubject> getSubject() {
        return Optional.ofNullable(getSubjectInternal());
    }

    @Override
    public int compareTo(GridRowData o) {
        ImmutableList<GridCellData> cells = getCells();
        ImmutableList<GridCellData> otherCells = o.getCells();
        for (int i = 0; i < cells.size() && i < otherCells.size(); i++) {
            GridCellData cellData = cells.get(i);
            GridCellData otherCellData = otherCells.get(i);
            int diff = cellData.compareTo(otherCellData);
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    @Nullable
    public FormSubject getSubjectInternal() {
        return subjectInternal;
    }

    @Nonnull
    public ImmutableList<GridCellData> getCells() {
        return cells;
    }
}
