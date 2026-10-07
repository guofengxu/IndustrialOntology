package org.industrial.ontology.domain.search;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkArgument;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntityNameMatchResult}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/11/2013
 */
public record EntityNameMatchResult(int start, int end, EntityNameMatchType matchType, PrefixNameMatchType prefixNameMatchType) implements Comparable<EntityNameMatchResult> {

    public EntityNameMatchResult {
        Objects.requireNonNull(matchType, "Null matchType");
        Objects.requireNonNull(prefixNameMatchType, "Null prefixNameMatchType");
    }

    public static EntityNameMatchResult get(int start, int end, EntityNameMatchType matchType, PrefixNameMatchType prefixNameMatchType) {
        checkArgument(start > -1);
        checkArgument(end > -1);
        checkArgument(start <= end);
        return new EntityNameMatchResult(start, end, matchType, prefixNameMatchType);
    }

    @Override
    public int compareTo(@Nonnull EntityNameMatchResult entityNameMatchResult) {
        final int typeDiff = this.getMatchType().compareTo(entityNameMatchResult.getMatchType());
        if (typeDiff != 0) {
            return typeDiff;
        }
        final int prefixNameMatchTypeDiff = this.getPrefixNameMatchType().compareTo(entityNameMatchResult.getPrefixNameMatchType());
        if (prefixNameMatchTypeDiff != 0) {
            return prefixNameMatchTypeDiff;
        }
        final int startDiff = this.getStart() - entityNameMatchResult.getStart();
        if (startDiff != 0) {
            return startDiff;
        }
        return this.getEnd() - entityNameMatchResult.getEnd();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public EntityNameMatchType getMatchType() {
        return matchType;
    }

    public PrefixNameMatchType getPrefixNameMatchType() {
        return prefixNameMatchType;
    }
}
