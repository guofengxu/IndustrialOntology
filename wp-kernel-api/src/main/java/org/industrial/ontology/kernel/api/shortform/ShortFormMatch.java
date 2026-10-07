package org.industrial.ontology.kernel.api.shortform;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ShortFormMatch}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 6 Apr 2018
 */
public record ShortFormMatch(@Nonnull OWLEntity entity, @Nonnull String shortForm, @Nonnull ImmutableList<ShortFormMatchPosition> matchPositions, @Nonnull DictionaryLanguage language) {

    public ShortFormMatch {
        java.util.Objects.requireNonNull(entity, "Null entity");
        java.util.Objects.requireNonNull(shortForm, "Null shortForm");
        java.util.Objects.requireNonNull(matchPositions, "Null matchPositions");
        java.util.Objects.requireNonNull(language, "Null language");
    }

    @Nonnull
    public static ShortFormMatch get(@Nonnull OWLEntity entity, @Nonnull String shortForm, @Nonnull DictionaryLanguage language, @Nonnull ImmutableList<ShortFormMatchPosition> shortFormMatchPositions) {
        for (var shortFormMatchPosition : shortFormMatchPositions) {
            if (!(shortFormMatchPosition.getStart() < shortForm.length())) {
                throw new IllegalArgumentException("Short form match start must be less than short from length");
            }
            if (!(shortFormMatchPosition.getEnd() <= shortForm.length())) {
                throw new IllegalArgumentException("Short form match end must be less than or equal to the short from length");
            }
        }
        return new ShortFormMatch(entity, shortForm, shortFormMatchPositions, language);
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    @Nonnull
    public String getShortForm() {
        return shortForm;
    }

    @Nonnull
    public ImmutableList<ShortFormMatchPosition> getMatchPositions() {
        return matchPositions;
    }

    @Nonnull
    public DictionaryLanguage getLanguage() {
        return language;
    }
}
