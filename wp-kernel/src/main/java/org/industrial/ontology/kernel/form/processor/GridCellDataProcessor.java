package org.industrial.ontology.kernel.form.processor;

import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.data.GridCellData;
import org.industrial.ontology.domain.form.field.OwlBinding;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridCellDataProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class GridCellDataProcessor {

    @Nonnull
    private final Supplier<FormControlDataProcessor> formControlDataProcessorProvider;

    public GridCellDataProcessor(@Nonnull Supplier<FormControlDataProcessor> formControlDataProcessorProvider) {
        this.formControlDataProcessorProvider = checkNotNull(formControlDataProcessorProvider);
    }

    public void processGridCellData(@Nonnull FormFrameBuilder rowFrameBuilder, @Nonnull OwlBinding cellBinding, @Nonnull GridCellData gridCellData) {
        var formControlDataProcessor = formControlDataProcessorProvider.get();
        gridCellData.getValues().forEach(cellControlData -> {
            formControlDataProcessor.processFormControlData(cellBinding, cellControlData, rowFrameBuilder);
        });
    }
}
