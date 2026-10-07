package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.EntityFormSelector}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-08
 */
public record EntityFormSelector(@JsonProperty("projectId") ProjectId projectId, @JsonProperty("criteria") CompositeRootCriteria criteria, @JsonProperty("formId") FormId formId) {

    public EntityFormSelector {
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(criteria, "Null criteria");
        Objects.requireNonNull(formId, "Null formId");
    }

    @JsonCreator
    public static EntityFormSelector get(@Nonnull @JsonProperty("projectId") ProjectId projectId, @Nonnull @JsonProperty("criteria") CompositeRootCriteria criteria, @Nonnull @JsonProperty("formId") FormId formId) {
        return new EntityFormSelector(projectId, criteria, formId);
    }

    @JsonProperty("projectId")
    public ProjectId getProjectId() {
        return projectId;
    }

    @JsonProperty("criteria")
    public CompositeRootCriteria getCriteria() {
        return criteria;
    }

    @JsonProperty("formId")
    public FormId getFormId() {
        return formId;
    }
}
