package org.industrial.ontology.domain.perspective;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-31
 */
public record PerspectiveDescriptor(@JsonProperty(PerspectiveDescriptor.PERSPECTIVE_ID) @Nonnull PerspectiveId perspectiveId, @JsonProperty(PerspectiveDescriptor.LABEL) @Nonnull LanguageMap label, @JsonProperty(PerspectiveDescriptor.FAVORITE) boolean favorite) {

    public PerspectiveDescriptor {
        Objects.requireNonNull(perspectiveId, "Null perspectiveId");
        Objects.requireNonNull(label, "Null label");
    }

    public static final String PERSPECTIVE_ID = "perspectiveId";

    public static final String LABEL = "label";

    public static final String FAVORITE = "favorite";

    @JsonCreator
    @Nonnull
    public static PerspectiveDescriptor get(@JsonProperty(PERSPECTIVE_ID) @Nonnull PerspectiveId perspectiveId, @JsonProperty(LABEL) @Nonnull LanguageMap newLabel, @JsonProperty(FAVORITE) boolean favorite) {
        return new PerspectiveDescriptor(perspectiveId, newLabel, favorite);
    }

    @Nonnull
    public PerspectiveDescriptor withFavorite(boolean favorite) {
        if (favorite == isFavorite()) {
            return this;
        }
        return PerspectiveDescriptor.get(getPerspectiveId(), getLabel(), favorite);
    }

    @JsonProperty(PERSPECTIVE_ID)
    @Nonnull
    public PerspectiveId getPerspectiveId() {
        return perspectiveId;
    }

    @JsonProperty(LABEL)
    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @JsonProperty(FAVORITE)
    public boolean isFavorite() {
        return favorite;
    }
}
