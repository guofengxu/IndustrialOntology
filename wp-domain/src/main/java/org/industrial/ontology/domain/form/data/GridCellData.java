package org.industrial.ontology.domain.form.data;

import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.pagination.Page;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridCellData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public record GridCellData(@Nonnull GridColumnId columnId, @Nonnull Page<FormControlData> values) {

    public GridCellData {
        Objects.requireNonNull(columnId, "Null columnId");
        Objects.requireNonNull(values, "Null values");
    }

    public static GridCellData get(@Nonnull GridColumnId columnId, @Nullable Page<FormControlData> values) {
        return new GridCellData(columnId, values);
    }

    public int compareTo(GridCellData otherCellData) {
        Page<FormControlData> valuesPage = getValues();
        Page<FormControlData> otherValuesPage = otherCellData.getValues();
        List<FormControlData> values = valuesPage.getPageElements();
        List<FormControlData> otherValues = otherValuesPage.getPageElements();
        for (int i = 0; i < values.size() && i < otherValues.size(); i++) {
            FormControlData formControlData = values.get(i);
            FormControlData otherControlData = otherValues.get(i);
            if (formControlData instanceof TextControlData && otherControlData instanceof TextControlData) {
                Optional<OWLLiteral> value = ((TextControlData) formControlData).getValue();
                Optional<OWLLiteral> otherValue = ((TextControlData) otherControlData).getValue();
                if (value.isPresent() && otherValue.isPresent()) {
                    int diff = value.get().compareTo(otherValue.get());
                    if (diff != 0) {
                        return diff;
                    }
                }
            }
            if (formControlData instanceof EntityNameControlData && otherControlData instanceof EntityNameControlData) {
                Optional<OWLEntity> value = ((EntityNameControlData) formControlData).getEntity();
                Optional<OWLEntity> otherValue = ((EntityNameControlData) otherControlData).getEntity();
                if (value.isPresent() && otherValue.isPresent()) {
                    int diff = value.get().compareTo(otherValue.get());
                    if (diff != 0) {
                        return diff;
                    }
                }
            }
        }
        return 0;
    }

    @Nonnull
    public GridColumnId getColumnId() {
        return columnId;
    }

    @Nonnull
    public Page<FormControlData> getValues() {
        return values;
    }
}
