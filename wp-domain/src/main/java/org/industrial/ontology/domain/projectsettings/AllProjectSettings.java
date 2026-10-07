package org.industrial.ontology.domain.projectsettings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.project.PrefixDeclaration;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.WithProjectId;
import org.industrial.ontology.domain.tag.Tag;
import javax.annotation.Nonnull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.projectsettings.AllProjectSettings}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-24
 */
public record AllProjectSettings(@JsonProperty(AllProjectSettings.PROJECT_SETTINGS) @Nonnull ProjectSettings projectSettings, @JsonProperty(AllProjectSettings.ENTITY_CREATION_SETTINGS) @Nonnull EntityCrudKitSettings entityCreationSettings, @JsonProperty(AllProjectSettings.PREFIX_DECLARATIONS) @Nonnull ImmutableList<PrefixDeclaration> prefixDeclarations, @JsonProperty(AllProjectSettings.PROJECT_TAGS) @Nonnull ImmutableList<Tag> projectTags) implements WithProjectId<AllProjectSettings> {

    public AllProjectSettings {
        Objects.requireNonNull(projectSettings, "Null projectSettings");
        Objects.requireNonNull(entityCreationSettings, "Null entityCreationSettings");
        Objects.requireNonNull(prefixDeclarations, "Null prefixDeclarations");
        Objects.requireNonNull(projectTags, "Null projectTags");
    }

    public static final String PROJECT_SETTINGS = "projectSettings";

    public static final String ENTITY_CREATION_SETTINGS = "entityCreationSettings";

    public static final String PREFIX_DECLARATIONS = "prefixDeclarations";

    public static final String PROJECT_TAGS = "projectTags";

    @JsonCreator
    @Nonnull
    public static AllProjectSettings get(@JsonProperty(PROJECT_SETTINGS) @Nonnull ProjectSettings projectSettings, @JsonProperty(ENTITY_CREATION_SETTINGS) @Nonnull EntityCrudKitSettings entityCrudKitSettings, @JsonProperty(PREFIX_DECLARATIONS) @Nonnull ImmutableList<PrefixDeclaration> prefixDeclarations, @JsonProperty(PROJECT_TAGS) @Nonnull ImmutableList<Tag> tags) {
        return new AllProjectSettings(projectSettings, entityCrudKitSettings, prefixDeclarations, tags);
    }

    @Override
    public AllProjectSettings withProjectId(@Nonnull ProjectId projectId) {
        return AllProjectSettings.get(getProjectSettings().withProjectId(projectId), getEntityCreationSettings(), getPrefixDeclarations(), getProjectTags().stream().map(t -> t.withProjectId(projectId)).collect(toImmutableList()));
    }

    @JsonProperty(PROJECT_SETTINGS)
    @Nonnull
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    @JsonProperty(ENTITY_CREATION_SETTINGS)
    @Nonnull
    public EntityCrudKitSettings getEntityCreationSettings() {
        return entityCreationSettings;
    }

    @JsonProperty(PREFIX_DECLARATIONS)
    @Nonnull
    public ImmutableList<PrefixDeclaration> getPrefixDeclarations() {
        return prefixDeclarations;
    }

    @JsonProperty(PROJECT_TAGS)
    @Nonnull
    public ImmutableList<Tag> getProjectTags() {
        return projectTags;
    }
}
