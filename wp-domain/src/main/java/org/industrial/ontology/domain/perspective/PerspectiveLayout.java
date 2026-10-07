package org.industrial.ontology.domain.perspective;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveLayout}.
 * <p>
 * The layout tree is kept as raw JSON (the legacy widgetmap {@code Node} format) so that layouts
 * saved by the old system round-trip unchanged; the web client converts it (docs/01 §5.3).
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-28
 */
public record PerspectiveLayout(@JsonProperty(PERSPECTIVE_ID) @Nonnull PerspectiveId perspectiveId, @JsonProperty(LAYOUT) @Nullable JsonNode layoutInternal) {

    public PerspectiveLayout {
        Objects.requireNonNull(perspectiveId, "Null perspectiveId");
    }

    public static final String PERSPECTIVE_ID = "perspectiveId";

    public static final String LAYOUT = "layout";

    @Nonnull
    public static PerspectiveLayout get(@Nonnull PerspectiveId perspectiveId, @Nonnull Optional<JsonNode> layout) {
        return getInternal(perspectiveId, layout.orElse(null));
    }

    @Nonnull
    public static PerspectiveLayout get(@Nonnull PerspectiveId perspectiveId, @Nonnull JsonNode layout) {
        return getInternal(perspectiveId, layout);
    }

    @Nonnull
    public static PerspectiveLayout get(@Nonnull PerspectiveId perspectiveId) {
        return getInternal(perspectiveId, null);
    }

    @JsonCreator
    @Nonnull
    protected static PerspectiveLayout getInternal(@JsonProperty(PERSPECTIVE_ID) @Nullable PerspectiveId perspectiveId, @JsonProperty(LAYOUT) @Nullable JsonNode layout) {
        return new PerspectiveLayout(perspectiveId, layout);
    }

    @JsonIgnore
    public Optional<JsonNode> getLayout() {
        return Optional.ofNullable(getLayoutInternal());
    }

    @Nonnull
    public PerspectiveId getPerspectiveId() {
        return perspectiveId;
    }

    @Nullable
    public JsonNode getLayoutInternal() {
        return layoutInternal;
    }
}
