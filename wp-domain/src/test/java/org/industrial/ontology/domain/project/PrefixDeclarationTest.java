package org.industrial.ontology.domain.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.PrefixDeclaration_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PrefixDeclarationTest {

    private static final String PREFIX_NAME = "p:";

    private static final String PREFIX = "http://prefix.com/prefix/";

    private PrefixDeclaration prefixDeclaration;

    @BeforeEach
    public void setUp() {
        prefixDeclaration = PrefixDeclaration.get(PREFIX_NAME, PREFIX);
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(prefixDeclaration, is(prefixDeclaration));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(prefixDeclaration.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(prefixDeclaration, is(PrefixDeclaration.get(PREFIX_NAME, PREFIX)));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(prefixDeclaration.hashCode(), is(PrefixDeclaration.get(PREFIX_NAME, PREFIX).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(prefixDeclaration.toString(), startsWith("PrefixDeclaration"));
    }

    @Test
    public void should_getPrefixName() {
        assertThat(prefixDeclaration.getPrefixName(), is(PREFIX_NAME));
    }

    @Test
    public void should_getPrefix() {
        assertThat(prefixDeclaration.getPrefix(), is(PREFIX));
    }

    @Test
    public void shouldNotPremitPrefixNameThatDoesNotEndWithColon() {
        assertThrows(IllegalArgumentException.class, () -> {
            PrefixDeclaration decl = PrefixDeclaration.get("a", "http://something.com/");
        });
    }

    @Test
    public void shouldReportSlashAsACommonTerminatingCharacter() {
        PrefixDeclaration decl = PrefixDeclaration.get("a:", "http://something.com/ont/");
        assertThat(decl.isPrefixWithCommonTerminatingCharacter(), is(true));
    }

    @Test
    public void shouldReportHashAsACommonTerminatingCharacter() {
        PrefixDeclaration decl = PrefixDeclaration.get("a:", "http://something.com/ont#");
        assertThat(decl.isPrefixWithCommonTerminatingCharacter(), is(true));
    }

    @Test
    public void shouldReportUnderscoreAsACommonTerminatingCharacter() {
        PrefixDeclaration decl = PrefixDeclaration.get("a:", "http://something.com/ont_");
        assertThat(decl.isPrefixWithCommonTerminatingCharacter(), is(true));
    }
}
