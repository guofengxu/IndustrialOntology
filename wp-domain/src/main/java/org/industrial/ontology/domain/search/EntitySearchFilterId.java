package org.industrial.ontology.domain.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import java.util.UUID;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntitySearchFilterId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-15
 */
public record EntitySearchFilterId(@Nonnull String id) {

    public EntitySearchFilterId {
        Objects.requireNonNull(id, "Null id");
    }

    /**
     * Gets a {@link EntitySearchFilter}
     * @param id The search filter id.  This must be a UUID in the standard UUID format.
     */
    @JsonCreator
    @Nonnull
    public static EntitySearchFilterId get(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(checkNotNull(id))) {
            throw new IllegalArgumentException("Malformed filter id: " + id);
        }
        return new EntitySearchFilterId(id);
    }

    /**
     * Create a new filter id using a random UUID.  This method only works
     * on the server.
     */
    @Nonnull
    public static EntitySearchFilterId createFilterId() {
        return get(UUID.randomUUID().toString());
    }

    @JsonValue
    @Nonnull
    public String getId() {
        return id;
    }
}
