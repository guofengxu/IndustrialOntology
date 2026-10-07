package org.industrial.ontology.domain.form.data;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.IRIData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.IriFormControlDataDto}.
 */
@AutoValue
public abstract class IriFormControlDataDto extends PrimitiveFormControlDataDto {

    public static IriFormControlDataDto get(@Nonnull IRIData iriData) {
        return new AutoValue_IriFormControlDataDto(iriData);
    }

    @Nonnull
    public abstract IRIData getIri();

    @Nonnull
    @Override
    public PrimitiveFormControlData toPrimitiveFormControlData() {
        return IriFormControlData.get(getIri().getObject());
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public OWLPrimitiveData getPrimitiveData() {
        return getIri();
    }
}
