package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.match.RootCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.EntityFormDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-22
 */
public record EntityFormDescriptor(@Nonnull @JsonProperty(EntityFormDescriptor.PROJECT_ID) ProjectId projectId, @Nonnull @JsonProperty(EntityFormDescriptor.FORM_ID) FormId formId, @Nonnull @JsonProperty(EntityFormDescriptor.FORM_DESCRIPTOR) FormDescriptor descriptor, @Nonnull @JsonProperty(EntityFormDescriptor.SELECTOR_CRITERIA) RootCriteria selectorCriteria) {

    public EntityFormDescriptor {
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(formId, "Null formId");
        Objects.requireNonNull(descriptor, "Null descriptor");
        Objects.requireNonNull(selectorCriteria, "Null selectorCriteria");
    }

    public static final String PROJECT_ID = "projectId";

    public static final String FORM_ID = "formId";

    public static final String FORM_DESCRIPTOR = "formDescriptor";

    public static final String SELECTOR_CRITERIA = "formSelectorCriteria";

    @JsonCreator
    public static EntityFormDescriptor valueOf(@JsonProperty(PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(FORM_ID) @Nonnull FormId formId, @JsonProperty(FORM_DESCRIPTOR) @Nonnull FormDescriptor newDescriptor, @JsonProperty(SELECTOR_CRITERIA) @Nonnull RootCriteria newSelectorCriteria) {
        return new EntityFormDescriptor(projectId, formId, newDescriptor, newSelectorCriteria);
    }

    @Nonnull
    @JsonProperty(PROJECT_ID)
    public ProjectId getProjectId() {
        return projectId;
    }

    @Nonnull
    @JsonProperty(FORM_ID)
    public FormId getFormId() {
        return formId;
    }

    @Nonnull
    @JsonProperty(FORM_DESCRIPTOR)
    public FormDescriptor getDescriptor() {
        return descriptor;
    }

    @Nonnull
    @JsonProperty(SELECTOR_CRITERIA)
    public RootCriteria getSelectorCriteria() {
        return selectorCriteria;
    }
}
