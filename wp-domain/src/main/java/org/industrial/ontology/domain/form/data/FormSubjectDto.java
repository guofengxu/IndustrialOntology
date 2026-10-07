package org.industrial.ontology.domain.form.data;




import org.industrial.ontology.domain.entity.IRIData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormSubjectDto}.
 */
public abstract class FormSubjectDto {

    @Nonnull
    public static FormIriSubjectDto get(@Nonnull IRIData iri) {
        return new AutoValue_FormIriSubjectDto(iri);
    }

    @Nonnull
    public static FormEntitySubjectDto get(@Nonnull OWLEntityData entity) {
        return new AutoValue_FormEntitySubjectDto(entity);
    }

    @Nonnull
    public abstract IRI getIri();

    @Nonnull
    public abstract FormSubject toFormSubject();

    public static FormSubjectDto getFormSubject(OWLPrimitiveData root) {
        if (root instanceof IRIData) {
            return FormSubjectDto.get((IRIData) root);
        } else if (root instanceof OWLEntityData) {
            return FormSubjectDto.get((OWLEntityData) root);
        } else {
            throw new RuntimeException("Cannot process form subjects that are not IRIs or Entities");
        }
    }
}
