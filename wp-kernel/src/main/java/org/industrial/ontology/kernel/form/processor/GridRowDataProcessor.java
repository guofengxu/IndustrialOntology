package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import org.industrial.ontology.domain.form.GridColumnBindingMissingException;
import org.industrial.ontology.domain.form.data.GridRowData;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridRowDataProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class GridRowDataProcessor {

    @Nonnull
    private final Supplier<FormFrameBuilder> formFrameBuilderProvider;

    @Nonnull
    private final GridCellDataProcessor gridCellDataProcessor;

    public GridRowDataProcessor(@Nonnull Supplier<FormFrameBuilder> formFrameBuilderProvider, @Nonnull GridCellDataProcessor gridCellDataProcessor) {
        this.formFrameBuilderProvider = checkNotNull(formFrameBuilderProvider);
        this.gridCellDataProcessor = checkNotNull(gridCellDataProcessor);
    }

    public void processGridRowData(@Nonnull OwlBinding binding, @Nonnull FormSubjectFactoryDescriptor rowSubjectFactoryDescriptor, @Nonnull ImmutableList<GridColumnDescriptor> columnDescriptors, @Nonnull FormFrameBuilder gridFrameBuilder, GridRowData gridRowData) {
        var rowFrameBuilder = formFrameBuilderProvider.get();
        var rowSubject = gridRowData.getSubject();
        // Set subject if it is present.  If it is not present
        // then a subject should be generated
        rowSubject.ifPresent(rowFrameBuilder::setSubject);
        rowFrameBuilder.setSubjectFactoryDescriptor(rowSubjectFactoryDescriptor);
        var rowCells = gridRowData.getCells();
        for (int i = 0; i < columnDescriptors.size(); i++) {
            var columnDescriptor = columnDescriptors.get(i);
            var gridCellData = rowCells.get(i);
            var columnId = columnDescriptor.getId();
            var cellBinding = columnDescriptor.getOwlBinding().orElseThrow(() -> new GridColumnBindingMissingException(columnId));
            gridCellDataProcessor.processGridCellData(rowFrameBuilder, cellBinding, gridCellData);
        }
        gridFrameBuilder.add(binding, rowFrameBuilder);
    }
}
