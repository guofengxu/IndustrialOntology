package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.form.FormPageRequest;
import org.industrial.ontology.domain.form.data.FormSubject;
import org.industrial.ontology.domain.form.field.FormRegionId;
import org.industrial.ontology.domain.pagination.PageRequest;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormPageRequestIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-22
 */
public class FormPageRequestIndex {

    @Nonnull
    private final ImmutableMap<Key, FormPageRequest> indexMap;

    public FormPageRequestIndex(@Nonnull ImmutableMap<Key, FormPageRequest> indexMap) {
        this.indexMap = checkNotNull(indexMap);
    }

    @Nonnull
    public static FormPageRequestIndex create(@Nonnull ImmutableList<FormPageRequest> pageRequests) {
        checkNotNull(pageRequests);
        Map<Key, FormPageRequest> map = new HashMap<>();
        for (FormPageRequest pageRequest : pageRequests) {
            map.put(Key.get(pageRequest.getSubject(), pageRequest.getFieldId(), pageRequest.getSourceType()), pageRequest);
        }
        return new FormPageRequestIndex(ImmutableMap.copyOf(map));
    }

    @Nonnull
    public PageRequest getPageRequest(FormSubject formSubject, FormRegionId id, FormPageRequest.SourceType sourceType) {
        var formPageRequest = indexMap.get(Key.get(formSubject, id, sourceType));
        if (formPageRequest != null) {
            return formPageRequest.getPageRequest();
        } else {
            return PageRequest.requestPageWithSize(1, FormPageRequest.DEFAULT_PAGE_SIZE);
        }
    }

    public record Key(@Nonnull FormSubject formSubject, @Nonnull FormRegionId formRegionId, @Nonnull FormPageRequest.SourceType sourceType) {

        public Key {
            Objects.requireNonNull(formSubject, "Null formSubject");
            Objects.requireNonNull(formRegionId, "Null formRegionId");
            Objects.requireNonNull(sourceType, "Null sourceType");
        }

        public static Key get(@Nonnull FormSubject subject, @Nonnull FormRegionId formRegionId, @Nonnull FormPageRequest.SourceType sourceType) {
            return new FormPageRequestIndex.Key(subject, formRegionId, sourceType);
        }

        @Nonnull
        public FormSubject getFormSubject() {
            return formSubject;
        }

        @Nonnull
        public FormRegionId getFormRegionId() {
            return formRegionId;
        }

        @Nonnull
        public FormPageRequest.SourceType getSourceType() {
            return sourceType;
        }
    }
}
