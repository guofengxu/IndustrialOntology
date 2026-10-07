package org.industrial.ontology.domain.lang;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LangTagFilter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public record LangTagFilter(@Nonnull ImmutableSet<LangTag> filteringTags) {

    public LangTagFilter {
        Objects.requireNonNull(filteringTags, "Null filteringTags");
    }

    @Nonnull
    public static LangTagFilter get(@Nonnull ImmutableSet<LangTag> filteredLangTags) {
        return new LangTagFilter(filteredLangTags);
    }

    public boolean isAnyLangTagIncluded() {
        return getFilteringTags().isEmpty();
    }

    /**
     * Determines whether there is at least one filtered lang tag
     * @return true if there is at least one filtered lang tag, otherwise false
     */
    public boolean isFilterActive() {
        return getFilteringTags().size() > 0;
    }

    /**
     * Determines if the specified langtag is included by this filter.
     * @param langTag The lang tag.
     */
    public boolean isIncluded(@Nonnull LangTag langTag) {
        ImmutableSet<LangTag> filteringTags = getFilteringTags();
        return filteringTags.isEmpty() || filteringTags.contains(langTag);
    }

    public boolean isIncluded(@Nonnull String langTag) {
        if (getFilteringTags().isEmpty()) {
            return true;
        }
        LangTag tag = LangTag.get(langTag);
        return isIncluded(tag);
    }

    @Nonnull
    public ImmutableSet<LangTag> getFilteringTags() {
        return filteringTags;
    }
}
