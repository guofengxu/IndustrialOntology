package org.industrial.ontology.domain.form.data;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.IRIData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormEntitySubjectDto}.
 */
@AutoValue
public abstract class FormEntitySubjectDto extends FormSubjectDto {

    @Nonnull
    public static FormEntitySubjectDto get(@Nonnull OWLEntityData entityData) {
        return new AutoValue_FormEntitySubjectDto(entityData);
    }

    @Nonnull
    public static FormIriSubjectDto get(@Nonnull IRIData iriData) {
        return new AutoValue_FormIriSubjectDto(iriData);
    }


    @Nonnull
    public abstract OWLEntityData getEntityData();

    @Nonnull
    @Override
    public IRI getIri() {
        return getEntityData().getEntity().getIRI();
    }

    @Nonnull
    @Override
    public FormSubject toFormSubject() {
        return FormEntitySubject.get(getEntityData().getEntity());
    }
}
