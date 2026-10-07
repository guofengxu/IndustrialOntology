package org.industrial.ontology.domain.tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.match.RootCriteria;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.TagData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23 Mar 2018
 */
public record TagData(@Nullable TagId _getTagId, @Nonnull String getLabel, @Nonnull String getDescription, @Nonnull Color getColor, @Nonnull Color getBackgroundColor, @Nonnull ImmutableList<RootCriteria> getCriteria, int getUsageCount) {

    public TagData {
        java.util.Objects.requireNonNull(getLabel, "Null getLabel");
        java.util.Objects.requireNonNull(getDescription, "Null getDescription");
        java.util.Objects.requireNonNull(getColor, "Null getColor");
        java.util.Objects.requireNonNull(getBackgroundColor, "Null getBackgroundColor");
        java.util.Objects.requireNonNull(getCriteria, "Null getCriteria");
    }

    public static TagData get(@Nonnull String label, @Nonnull String description, @Nonnull Color color, @Nonnull Color backgroundColor, @Nonnull ImmutableList<RootCriteria> criteria, int usageCount) {
        return get(null, label, description, color, backgroundColor, criteria, usageCount);
    }

    @JsonCreator
    public static TagData get(@Nullable TagId tagId, @Nonnull String label, @Nonnull String description, @Nonnull Color color, @Nonnull Color backgroundColor, @Nonnull ImmutableList<RootCriteria> criteria, int usageCount) {
        return new TagData(tagId, label, description, color, backgroundColor, criteria, usageCount);
    }

    @Nonnull
    public Optional<TagId> getTagId() {
        return Optional.ofNullable(_getTagId());
    }

    public TagData withCriteria(ImmutableList<RootCriteria> criteria) {
        return get(_getTagId(), getLabel(), getDescription(), getColor(), getBackgroundColor(), criteria, getUsageCount());
    }
}
