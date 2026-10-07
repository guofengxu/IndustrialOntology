package org.industrial.ontology.kernel.form.processor;



import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptorMissingException;
import org.industrial.ontology.domain.form.data.GridControlData;
import org.industrial.ontology.domain.form.data.GridRowData;
import org.industrial.ontology.domain.form.field.OwlBinding;

import javax.annotation.Nonnull;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridControlDataProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class GridControlDataProcessor {

    @Nonnull
    private final GridRowDataProcessor gridRowDataProcessor;

    public GridControlDataProcessor(@Nonnull GridRowDataProcessor gridRowDataProcessor) {
        this.gridRowDataProcessor = gridRowDataProcessor;
    }

    public void processGridControlData(@Nonnull OwlBinding binding,
                                       @Nonnull GridControlData gridControlData,
                                       @Nonnull FormFrameBuilder formFrameBuilder) {
        var gridControlDescriptor = gridControlData.getDescriptor();
        // The subject factory descriptor MUST exists otherwise the form descriptor is not properly configured
        var rowSubjectFactoryDescriptor = gridControlDescriptor.getSubjectFactoryDescriptor()
                                                               .orElseThrow(FormSubjectFactoryDescriptorMissingException::new);
        for(GridRowData gridRowData : gridControlData.getRows()) {
            gridRowDataProcessor.processGridRowData(binding,
                                                    rowSubjectFactoryDescriptor,
                                                    gridControlDescriptor.getColumns(),
                                                    formFrameBuilder, gridRowData);
        }
    }
}
