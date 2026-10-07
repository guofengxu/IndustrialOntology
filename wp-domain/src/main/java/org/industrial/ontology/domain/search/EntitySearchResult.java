package org.industrial.ontology.domain.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.EntityNode;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntitySearchResult}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-24
 */
public record EntitySearchResult(@JsonProperty(EntitySearchResult.ENTITY) @Nonnull EntityNode entity, @JsonProperty(EntitySearchResult.MATCHES) @Nonnull ImmutableList<SearchResultMatch> matches) {

    public EntitySearchResult {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(matches, "Null matches");
    }

    public static final String ENTITY = "entity";

    public static final String MATCHES = "matches";

    @Nonnull
    public static EntitySearchResult get(@JsonProperty(ENTITY) @Nonnull EntityNode entity, @JsonProperty(MATCHES) @Nonnull ImmutableList<SearchResultMatch> matches) {
        return new EntitySearchResult(entity, matches);
    }

    @JsonProperty(ENTITY)
    @Nonnull
    public EntityNode getEntity() {
        return entity;
    }

    /**
     * Get the matches for this particular entity
     * @return The list of matches
     */
    @JsonProperty(MATCHES)
    @Nonnull
    public ImmutableList<SearchResultMatch> getMatches() {
        return matches;
    }
}
