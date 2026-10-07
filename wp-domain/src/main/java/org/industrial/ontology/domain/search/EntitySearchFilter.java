package org.industrial.ontology.domain.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntitySearchFilter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-15
 */
public record EntitySearchFilter(@JsonProperty(EntitySearchFilter.ID) @Nonnull EntitySearchFilterId id, @JsonProperty(EntitySearchFilter.PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(EntitySearchFilter.LABEL) @Nonnull LanguageMap label, @JsonProperty(EntitySearchFilter.CRITERIA) @Nonnull EntityMatchCriteria entityMatchCriteria) {

    public EntitySearchFilter {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(entityMatchCriteria, "Null entityMatchCriteria");
    }

    public static final String ID = "_id";

    public static final String PROJECT_ID = "projectId";

    public static final String LABEL = "label";

    public static final String CRITERIA = "criteria";

    @JsonCreator
    @Nonnull
    public static EntitySearchFilter get(@JsonProperty(ID) @Nonnull EntitySearchFilterId id, @JsonProperty(PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(LABEL) @Nonnull LanguageMap label, @JsonProperty(CRITERIA) @Nonnull EntityMatchCriteria entityMatchCriteria) {
        return new EntitySearchFilter(id, projectId, label, entityMatchCriteria);
    }

    @JsonProperty(ID)
    @Nonnull
    public EntitySearchFilterId getId() {
        return id;
    }

    @JsonProperty(PROJECT_ID)
    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    @JsonProperty(LABEL)
    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @JsonProperty(CRITERIA)
    @Nonnull
    public EntityMatchCriteria getEntityMatchCriteria() {
        return entityMatchCriteria;
    }
}
