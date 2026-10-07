package org.industrial.ontology.domain.form;

import org.industrial.ontology.domain.form.data.FormSubject;
import org.industrial.ontology.domain.form.field.FormRegionId;
import org.industrial.ontology.domain.pagination.PageRequest;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormPageRequest}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-22
 */
public record FormPageRequest(@Nonnull FormId formId, @Nonnull FormSubject subject, @Nonnull FormRegionId fieldId, @Nonnull SourceType sourceType, @Nonnull PageRequest pageRequest) {

    public FormPageRequest {
        Objects.requireNonNull(formId, "Null formId");
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(fieldId, "Null fieldId");
        Objects.requireNonNull(sourceType, "Null sourceType");
        Objects.requireNonNull(pageRequest, "Null pageRequest");
    }

    public static final int DEFAULT_PAGE_SIZE = 10;

    public enum SourceType {

        CONTROL_STACK, GRID_CONTROL
    }

    @Nonnull
    public static FormPageRequest get(@Nonnull FormId formId, @Nonnull FormSubject subject, @Nonnull FormRegionId formFieldId, @Nonnull SourceType sourceType, @Nonnull PageRequest pageRequest) {
        return new FormPageRequest(formId, subject, formFieldId, sourceType, pageRequest);
    }

    @Nonnull
    public FormId getFormId() {
        return formId;
    }

    @Nonnull
    public FormSubject getSubject() {
        return subject;
    }

    @Nonnull
    public FormRegionId getFieldId() {
        return fieldId;
    }

    @Nonnull
    public SourceType getSourceType() {
        return sourceType;
    }

    @Nonnull
    public PageRequest getPageRequest() {
        return pageRequest;
    }
}
