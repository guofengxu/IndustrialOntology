package org.industrial.ontology.app.perspective.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.perspective.PerspectiveDescriptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument.PERSPECTIVES;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument.PROJECT_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument.USER_ID;

/**
 * A document of the {@code PerspectiveDescriptors} collection; ported from the legacy server-side
 * {@code PerspectiveDescriptorsRecord} and written through Jackson. The application-wide list has neither a project
 * nor a user, a project's default list has no user, and a user's own list has both; the missing ones are written as
 * explicit {@code null}s.
 */
@JsonPropertyOrder({PROJECT_ID, USER_ID, PERSPECTIVES})
public record PerspectiveDescriptorsDocument(@JsonProperty(PROJECT_ID) @Nullable ProjectId projectId,
                                             @JsonProperty(USER_ID) @Nullable UserId userId,
                                             @JsonProperty(PERSPECTIVES)
                                             @Nonnull ImmutableList<PerspectiveDescriptor> perspectives) {

    public static final String PROJECT_ID = "projectId";

    public static final String USER_ID = "userId";

    public static final String PERSPECTIVES = "perspectives";

    public PerspectiveDescriptorsDocument {
        perspectives = perspectives == null ? ImmutableList.of() : perspectives;
    }

    @Nonnull
    public static PerspectiveDescriptorsDocument get(@Nonnull ImmutableList<PerspectiveDescriptor> perspectives) {
        return new PerspectiveDescriptorsDocument(null, null, perspectives);
    }

    @Nonnull
    public static PerspectiveDescriptorsDocument get(@Nonnull ProjectId projectId,
                                                     @Nonnull ImmutableList<PerspectiveDescriptor> perspectives) {
        return new PerspectiveDescriptorsDocument(projectId, null, perspectives);
    }

    @Nonnull
    public static PerspectiveDescriptorsDocument get(@Nonnull ProjectId projectId,
                                                     @Nonnull UserId userId,
                                                     @Nonnull ImmutableList<PerspectiveDescriptor> perspectives) {
        return new PerspectiveDescriptorsDocument(projectId, userId, perspectives);
    }
}
