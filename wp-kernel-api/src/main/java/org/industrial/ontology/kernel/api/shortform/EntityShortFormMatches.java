package org.industrial.ontology.kernel.api.shortform;

import com.google.common.collect.ImmutableList;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityShortFormMatches}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-13
 */
public record EntityShortFormMatches(@Nonnull OWLEntity entity, @Nonnull ImmutableList<ShortFormMatch> shortFormMatches) {

    public EntityShortFormMatches {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(shortFormMatches, "Null shortFormMatches");
    }

    public static EntityShortFormMatches get(@Nonnull OWLEntity entity, @Nonnull ImmutableList<ShortFormMatch> shortFormMatches) {
        checkShortFormMatchEntities(entity, shortFormMatches);
        return new EntityShortFormMatches(entity, shortFormMatches);
    }

    private static void checkShortFormMatchEntities(@Nonnull OWLEntity entity, @Nonnull ImmutableList<ShortFormMatch> shortFormMatches) {
        for (var shortFormMatch : shortFormMatches) {
            if (!shortFormMatch.getEntity().equals(entity)) {
                throw new IllegalArgumentException(String.format("Short form entity (%s) does not match main entity (%s)", shortFormMatch.getEntity(), entity));
            }
        }
    }

    /**
     * Get the entity whose short forms was matched
     */
    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    /**
     * Gets the short form matches for the entity
     */
    @Nonnull
    public ImmutableList<ShortFormMatch> getShortFormMatches() {
        return shortFormMatches;
    }
}
