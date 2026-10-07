package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-06
 */
public record FormData(@JsonIgnore @Nullable FormSubject subjectInternal, FormDescriptor formDescriptor, ImmutableList<FormFieldData> formFieldData) implements FormControlData {

    public FormData {
        Objects.requireNonNull(formDescriptor, "Null formDescriptor");
        Objects.requireNonNull(formFieldData, "Null formFieldData");
    }

    public static FormData get(@Nonnull Optional<FormSubject> subject, @Nonnull FormDescriptor formDescriptor, @Nonnull ImmutableList<FormFieldData> formFieldData) {
        return new FormData(subject.orElse(null), formDescriptor, formFieldData);
    }

    public static FormData empty(@Nonnull OWLEntity entity, @Nonnull FormId formId) {
        return get(Optional.of(FormSubject.get(entity)), FormDescriptor.empty(formId), ImmutableList.of());
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @Nonnull
    public Optional<FormSubject> getSubject() {
        return Optional.ofNullable(getSubjectInternal());
    }

    @JsonIgnore
    @Nullable
    public FormSubject getSubjectInternal() {
        return subjectInternal;
    }

    public FormDescriptor getFormDescriptor() {
        return formDescriptor;
    }

    public ImmutableList<FormFieldData> getFormFieldData() {
        return formFieldData;
    }
}
