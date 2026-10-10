package org.industrial.ontology.app.project;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageUsage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The language settings of a project and the languages its annotations use, what the legacy
 * {@code GetProjectInfo} gave every viewer for the language settings: the project settings' languages and the
 * {@code ActiveLanguagesManager}'s language usage.
 *
 * @param languageUsage the languages in use, most used first
 */
public record ProjectLanguages(@Nonnull DictionaryLanguage defaultLanguage,
                               @Nonnull DisplayNameSettings displayNameSettings,
                               @Nonnull List<DictionaryLanguageUsage> languageUsage) {

    public ProjectLanguages {
        checkNotNull(defaultLanguage);
        checkNotNull(displayNameSettings);
        languageUsage = ImmutableList.copyOf(languageUsage);
    }
}
