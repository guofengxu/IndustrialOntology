package org.industrial.ontology.domain.perspective;

import org.industrial.ontology.domain.lang.LanguageMap;
import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveDetails}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-09-02
 */
public record PerspectiveDetails(@Nonnull PerspectiveId perspectiveId, @Nonnull LanguageMap label, boolean favorite, @Nullable JsonNode layoutInternal) {

    public PerspectiveDetails {
        Objects.requireNonNull(perspectiveId, "Null perspectiveId");
        Objects.requireNonNull(label, "Null label");
    }

    @Nonnull
    public static PerspectiveDetails get(@Nonnull PerspectiveId perspectiveId, @Nonnull LanguageMap label, boolean favorite, @Nullable JsonNode layout) {
        return new PerspectiveDetails(perspectiveId, label, favorite, layout);
    }

    @Nonnull
    public Optional<JsonNode> getLayout() {
        return Optional.ofNullable(getLayoutInternal());
    }

    public PerspectiveDescriptor toPerspectiveDescriptor() {
        return PerspectiveDescriptor.get(getPerspectiveId(), getLabel(), isFavorite());
    }

    @Nonnull
    public PerspectiveId getPerspectiveId() {
        return perspectiveId;
    }

    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    public boolean isFavorite() {
        return favorite;
    }

    @Nullable
    public JsonNode getLayoutInternal() {
        return layoutInternal;
    }
}
