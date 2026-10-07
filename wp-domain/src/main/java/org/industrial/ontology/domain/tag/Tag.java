package org.industrial.ontology.domain.tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.match.RootCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.WithProjectId;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import static com.google.common.base.Preconditions.checkArgument;
import static org.industrial.ontology.domain.core.DeserializationUtil.nonNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.Tag}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Mar 2018
 *
 * Represents a tag in a project.  Tags are used to tag entities with information that can be used for
 * development and project management.
 */
public record Tag(@JsonProperty(Tag.ID) @Nonnull TagId tagId, @JsonProperty(Tag.PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(Tag.LABEL) @Nonnull String label, @JsonProperty(Tag.DESCRIPTION) @Nonnull String description, @JsonProperty(Tag.COLOR) @Nonnull Color color, @JsonProperty(Tag.BACKGROUND_COLOR) @Nonnull Color backgroundColor, @JsonProperty(Tag.CRITERIA) @Nonnull ImmutableList<RootCriteria> criteria) implements WithProjectId<Tag> {

    public Tag {
        Objects.requireNonNull(tagId, "Null tagId");
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(description, "Null description");
        Objects.requireNonNull(color, "Null color");
        Objects.requireNonNull(backgroundColor, "Null backgroundColor");
        Objects.requireNonNull(criteria, "Null criteria");
    }

    public static final String ID = "_id";

    public static final String PROJECT_ID = "projectId";

    public static final String LABEL = "label";

    private static final String DESCRIPTION = "description";

    private static final String COLOR = "color";

    private static final String BACKGROUND_COLOR = "backgroundColor";

    private static final String CRITERIA = "criteria";

    /**
     * Creates a Tag.
     *
     * @param tagId           The tag id.
     * @param projectId       The project id of the project that the tag belongs to.
     * @param label           The label for the tag.  This must not be empty.
     * @param description     An optional description for the tag.  This may be empty.
     * @param color           A color for the tag.  This is the foreground color of the tag in the user interface.
     * @param backgroundColor A background color for the tag.  This is the background color of the tag in the user
     *                        interface.
     */
    @JsonCreator
    public static Tag get(@Nonnull @JsonProperty(ID) TagId tagId, @Nonnull @JsonProperty(PROJECT_ID) ProjectId projectId, @Nonnull @JsonProperty(LABEL) String label, @Nonnull @JsonProperty(DESCRIPTION) String description, @Nonnull @JsonProperty(COLOR) Color color, @Nonnull @JsonProperty(BACKGROUND_COLOR) Color backgroundColor, @Nullable @JsonProperty(CRITERIA) List<RootCriteria> criteria) {
        checkArgument(!label.isEmpty(), "Tag label cannot be empty");
        ImmutableList<RootCriteria> rootCriteria;
        if (criteria != null) {
            rootCriteria = ImmutableList.copyOf(criteria);
        } else {
            rootCriteria = ImmutableList.of();
        }
        return new Tag(tagId, projectId, nonNull(label), nonNull(description), color, backgroundColor, rootCriteria);
    }

    @Override
    public Tag withProjectId(@Nonnull ProjectId projectId) {
        return Tag.get(getTagId(), projectId, getLabel(), getDescription(), getColor(), getBackgroundColor(), getCriteria());
    }

    /**
     * Gets the {@link TagId}
     */
    @JsonProperty(ID)
    @Nonnull
    public TagId getTagId() {
        return tagId;
    }

    /**
     * Gets the project that this tag belongs to.
     */
    @JsonProperty(PROJECT_ID)
    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    /**
     * Gets the human readable name for the tag.
     */
    @JsonProperty(LABEL)
    @Nonnull
    public String getLabel() {
        return label;
    }

    /**
     * Gets a description for this tag.
     *
     * @return The description, possibly empty.
     */
    @JsonProperty(DESCRIPTION)
    @Nonnull
    public String getDescription() {
        return description;
    }

    /**
     * Gets the (foreground) color of the tag.
     *
     * @return The color as a hexadecimal string
     */
    @JsonProperty(COLOR)
    @Nonnull
    public Color getColor() {
        return color;
    }

    /**
     * Gets the background color of this tag.
     *
     * @return The background color as a hexadecimal string.
     */
    @JsonProperty(BACKGROUND_COLOR)
    @Nonnull
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @JsonProperty(CRITERIA)
    @Nonnull
    public ImmutableList<RootCriteria> getCriteria() {
        return criteria;
    }
}
