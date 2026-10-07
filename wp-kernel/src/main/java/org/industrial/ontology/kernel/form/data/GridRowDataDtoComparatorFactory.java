package org.industrial.ontology.kernel.form.data;



import org.industrial.ontology.kernel.form.FormRegionOrderingIndex;
import org.industrial.ontology.domain.form.data.GridRowDataDto;
import javax.annotation.Nonnull;

import java.util.Comparator;
import java.util.Objects;

import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;

import org.industrial.ontology.domain.form.field.FormRegionOrderingDirection;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.GridRowDataDtoComparatorFactory}.
 */
public class GridRowDataDtoComparatorFactory {


    private static final int FIRST_COLUMN = 0;

    @Nonnull
    private final GridCellDataDtoComparator cellComparator;

    @Nonnull
    private final FormRegionOrderingIndex orderingIndex;

    public GridRowDataDtoComparatorFactory(@Nonnull GridCellDataDtoComparator cellComparator,
                                           @Nonnull FormRegionOrderingIndex orderingIndex) {
        this.cellComparator = checkNotNull(cellComparator);
        this.orderingIndex = checkNotNull(orderingIndex);
    }

    @Nonnull
    public Comparator<GridRowDataDto> get(@Nonnull GridControlDescriptor descriptor,
                                          Optional<FormRegionOrderingDirection> directionOverride) {
        return orderingIndex.getOrderings()
                            .stream()
                            .filter(this::isColumnOrdering)
                            .map(ordering -> {
                                var leafColumnToTopLevelColumnMap = descriptor.getLeafColumnToTopLevelColumnMap();
                                var leafColumnId = (GridColumnId) ordering.getRegionId();
                                var topLevelColumnId = leafColumnToTopLevelColumnMap.get(leafColumnId);
                                int columnIndex = descriptor.getColumnIndex(topLevelColumnId);
                                if (columnIndex == -1) {
                                    return null;
                                }
                                var direction = directionOverride.orElse(ordering.getDirection());
                                return new GridRowDtoByColumnIndexComparator(
                                        cellComparator,
                                        direction,
                                        columnIndex);
                            })
                            .filter(Objects::nonNull)
                            .map(c -> (Comparator<GridRowDataDto>) c)
                            .reduce(Comparator::thenComparing)
                            // Compare by the first column
                            .orElseGet(() -> new GridRowDtoByColumnIndexComparator(cellComparator,
                                                                                   FormRegionOrderingDirection.ASC,
                                                                                   FIRST_COLUMN));
    }

    private boolean isColumnOrdering(FormRegionOrdering ordering) {
        return ordering.getRegionId() instanceof GridColumnId;
    }
}
