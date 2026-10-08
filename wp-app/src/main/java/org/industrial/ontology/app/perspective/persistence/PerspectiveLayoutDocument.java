package org.industrial.ontology.app.perspective.persistence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.perspective.PerspectiveId;
import org.industrial.ontology.domain.perspective.PerspectiveLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.LAYOUT;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.PERSPECTIVE_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.PROJECT_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.USER_ID;

/**
 * A document of the {@code PerspectiveLayouts} collection; ported from the legacy server-side
 * {@code PerspectiveLayoutRecord} and written through Jackson. The legacy layout was a widgetmap {@code Node} tree;
 * here it is kept as the JSON it was stored as (docs/01 §5.3: "JSON 原样", the front end converts it), including the
 * generated leaf ids. A {@code null} layout means the built-in one.
 */
@JsonPropertyOrder({PROJECT_ID, USER_ID, PERSPECTIVE_ID, LAYOUT})
public record PerspectiveLayoutDocument(@JsonProperty(PROJECT_ID) @Nullable ProjectId projectId,
                                        @JsonProperty(USER_ID) @Nullable UserId userId,
                                        @JsonProperty(PERSPECTIVE_ID) @Nonnull PerspectiveId perspectiveId,
                                        @JsonProperty(LAYOUT) @Nullable JsonNode layout) {

    public static final String PROJECT_ID = "projectId";

    public static final String USER_ID = "userId";

    public static final String PERSPECTIVE_ID = "perspectiveId";

    public static final String LAYOUT = "layout";

    public PerspectiveLayoutDocument {
        Objects.requireNonNull(perspectiveId, "perspectiveId");
        layout = layout == null || layout.isNull() ? null : layout;
    }

    @Nonnull
    public PerspectiveLayout toPerspectiveLayout() {
        return layout == null ? PerspectiveLayout.get(perspectiveId) : PerspectiveLayout.get(perspectiveId, layout);
    }
}
