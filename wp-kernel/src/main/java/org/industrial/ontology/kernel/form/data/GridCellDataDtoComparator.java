package org.industrial.ontology.kernel.form.data;



import com.google.common.collect.Comparators;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.GridCellDataDto;

import javax.annotation.Nonnull;
import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.GridCellDataDtoComparator}.
 */
public class GridCellDataDtoComparator implements Comparator<GridCellDataDto> {

    @Nonnull
    private Comparator<Iterable<FormControlDataDto>> lexComparator;

    public GridCellDataDtoComparator(@Nonnull FormControlDataDtoComparator formControlDataDtoComparator) {
        this.lexComparator = Comparators.lexicographical(formControlDataDtoComparator);
    }

    @Override
    public int compare(GridCellDataDto cellData1, GridCellDataDto cellData2) {
        var valuesPage = cellData1.getValues().getPageElements();
        var otherValuesPage = cellData2.getValues().getPageElements();
        return lexComparator.compare(valuesPage, otherValuesPage);
    }

}
