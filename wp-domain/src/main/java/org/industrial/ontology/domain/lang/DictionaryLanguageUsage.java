package org.industrial.ontology.domain.lang;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.DictionaryLanguageUsage}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21 Aug 2018
 */
public record DictionaryLanguageUsage(@Nonnull DictionaryLanguage dictionaryLanguage, int referenceCount) {

    public DictionaryLanguageUsage {
        Objects.requireNonNull(dictionaryLanguage, "Null dictionaryLanguage");
    }

    public static DictionaryLanguageUsage get(@Nonnull DictionaryLanguage language, int referenceCount) {
        return new DictionaryLanguageUsage(language, referenceCount);
    }

    @Nonnull
    public DictionaryLanguage getDictionaryLanguage() {
        return dictionaryLanguage;
    }

    public int getReferenceCount() {
        return referenceCount;
    }
}
