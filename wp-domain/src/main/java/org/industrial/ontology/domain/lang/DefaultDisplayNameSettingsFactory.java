package org.industrial.ontology.domain.lang;



import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.DefaultDisplayNameSettingsFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Sep 2018
 */
public class DefaultDisplayNameSettingsFactory {

    public DefaultDisplayNameSettingsFactory() {
    }

    @Nonnull
    public DisplayNameSettings getDefaultDisplayNameSettings(@Nonnull String langTag) {
        ImmutableList<DictionaryLanguage> primaryLanguages;
        if (langTag.isEmpty()) {
            primaryLanguages = ImmutableList.of(DictionaryLanguage.rdfsLabel(langTag),
                                                DictionaryLanguage.prefixedName(),
                                                DictionaryLanguage.localName());
        }
        else {
            primaryLanguages = ImmutableList.of(DictionaryLanguage.rdfsLabel(langTag),
                                                DictionaryLanguage.rdfsLabel(""),
                                                DictionaryLanguage.prefixedName(),
                                                DictionaryLanguage.localName());
        }
        return DisplayNameSettings.get(
                primaryLanguages,
                ImmutableList.of()
        );
    }
}
