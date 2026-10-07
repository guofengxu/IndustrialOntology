package org.industrial.ontology.domain.form.data;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.EntityFormControlDataDto}.
 */
@AutoValue
public abstract class EntityFormControlDataDto extends PrimitiveFormControlDataDto {

    @Nonnull
    public abstract OWLEntityData getEntity();

    @Nonnull
    @Override
    public PrimitiveFormControlData toPrimitiveFormControlData() {
        return EntityFormControlData.get(getEntity().getEntity());
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.empty();
    }

    @Override
    public boolean isDeprecated() {
        return getEntity().isDeprecated();
    }

    @Nonnull
    @Override
    public OWLPrimitiveData getPrimitiveData() {
        return getEntity();
    }
}
