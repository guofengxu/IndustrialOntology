package org.industrial.ontology.domain.form.data;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLLiteralData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.LiteralFormControlDataDto}.
 */
@AutoValue
public abstract class LiteralFormControlDataDto extends PrimitiveFormControlDataDto {

    @Nonnull
    public abstract OWLLiteralData getLiteral();

    @Nonnull
    @Override
    public PrimitiveFormControlData toPrimitiveFormControlData() {
        return LiteralFormControlData.get(getLiteral().getLiteral());
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.of(getLiteral().getLiteral());
    }

    @Nonnull
    @Override
    public OWLPrimitiveData getPrimitiveData() {
        return getLiteral();
    }
}
