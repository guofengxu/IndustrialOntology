package org.industrial.ontology.domain.lang;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.DefaultDisplayNameSettingsFactory_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Sep 2018
 */
public class DefaultDisplayNameSettingsFactoryTest {

    private DefaultDisplayNameSettingsFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new DefaultDisplayNameSettingsFactory();
    }

    @Test
    public void shouldCreateDefaultSettingsForEmptyLang() {
        DisplayNameSettings settings = factory.getDefaultDisplayNameSettings("");
        assertThat(settings.getPrimaryDisplayNameLanguages(), is(ImmutableList.of(DictionaryLanguage.rdfsLabel(""), DictionaryLanguage.prefixedName(), DictionaryLanguage.localName())));
    }

    @Test
    public void shouldCreateDefaultSettingsForNonEmptyLang() {
        DisplayNameSettings settings = factory.getDefaultDisplayNameSettings("en-GB");
        assertThat(settings.getPrimaryDisplayNameLanguages(), is(ImmutableList.of(DictionaryLanguage.rdfsLabel("en-GB"), DictionaryLanguage.rdfsLabel(""), DictionaryLanguage.prefixedName(), DictionaryLanguage.localName())));
    }
}
