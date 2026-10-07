package org.industrial.ontology.domain.form;

import org.industrial.ontology.domain.form.data.FormSubject;
import org.industrial.ontology.domain.form.field.FormRegionId;
import org.industrial.ontology.domain.pagination.PageRequest;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormRegionPageRequest}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-22
 */
public record FormRegionPageRequest(@Nonnull FormSubject subject, @Nonnull FormRegionId fieldId, @Nonnull FormPageRequest.SourceType sourceType, @Nonnull PageRequest pageRequest) {

    public FormRegionPageRequest {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(fieldId, "Null fieldId");
        Objects.requireNonNull(sourceType, "Null sourceType");
        Objects.requireNonNull(pageRequest, "Null pageRequest");
    }

    @Nonnull
    public static FormRegionPageRequest get(@Nonnull FormSubject subject, @Nonnull FormRegionId formRegionId, @Nonnull FormPageRequest.SourceType sourceType, @Nonnull PageRequest pageRequest) {
        return new FormRegionPageRequest(subject, formRegionId, sourceType, pageRequest);
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
    public FormPageRequest.SourceType getSourceType() {
        return sourceType;
    }

    @Nonnull
    public PageRequest getPageRequest() {
        return pageRequest;
    }
}
