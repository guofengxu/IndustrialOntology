package org.industrial.ontology.domain.perspective;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.UUID;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12/02/16
 */
public record PerspectiveId(@Nonnull String id) implements Serializable {

    public PerspectiveId {
        Objects.requireNonNull(id, "Null id");
    }

    @JsonCreator
    @Nonnull
    public static PerspectiveId get(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(id)) {
            throw new IllegalArgumentException("Malformed PerspectiveId.  PerspectiveIds must be UUIDs");
        }
        return new PerspectiveId(id);
    }

    public static PerspectiveId generate() {
        return get(UUID.randomUUID().toString());
    }

    /**
     * Gets the identifier for this perspective.  This is a human readable name.
     */
    @JsonValue
    @Nonnull
    public String getId() {
        return id;
    }
}
