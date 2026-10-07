package org.industrial.ontology.domain.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LanguageCodeParser_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/04/16
 */
public class LanguageCodeParserTest {

    private LanguageCodeParser parser;

    @BeforeEach
    public void setUp() throws Exception {
        parser = new LanguageCodeParser();
    }

    @Test
    public void shouldParseInput() {
        String input = "\"en\",\"English\"\n" + "\"fr\",\"French\"\n";
        Collection<LanguageCode> codes = parser.parse(input);
        assertThat(codes, hasItem(new LanguageCode("en", "English")));
        assertThat(codes, hasItem(new LanguageCode("fr", "French")));
    }

    @Test
    public void shouldSkipMalformedLines() {
        String input = "\"e\",\"English\"\n" + "\"fr\",\"French\"\n";
        Collection<LanguageCode> codes = parser.parse(input);
        assertThat(codes, not(hasItem(new LanguageCode("en", "English"))));
        assertThat(codes, hasItem(new LanguageCode("fr", "French")));
    }
}
