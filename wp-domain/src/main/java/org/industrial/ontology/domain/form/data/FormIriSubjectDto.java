package org.industrial.ontology.domain.form.data;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.IRIData;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormIriSubjectDto}.
 */
@AutoValue
public abstract class FormIriSubjectDto extends FormSubjectDto {

    @Nonnull
    public static FormIriSubjectDto get(@Nonnull IRIData data) {
        return new AutoValue_FormIriSubjectDto(data);
    }

    @Nonnull
    public abstract IRIData getIriData();

    @Nonnull
    @Override
    public IRI getIri() {
        return getIriData().getObject();
    }

    @Nonnull
    @Override
    public FormSubject toFormSubject() {
        return FormIriSubject.get(getIri());
    }
}
