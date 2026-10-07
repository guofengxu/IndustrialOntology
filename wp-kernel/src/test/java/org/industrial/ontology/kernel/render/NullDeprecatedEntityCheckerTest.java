package org.industrial.ontology.kernel.render;

import org.industrial.ontology.kernel.mansyntax.render.NullDeprecatedEntityChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.NullDeprecatedEntityChecker_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 29/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NullDeprecatedEntityCheckerTest {

    private NullDeprecatedEntityChecker checker;

    @BeforeEach
    public void setUp() throws Exception {
        checker = NullDeprecatedEntityChecker.get();
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(checker, is(equalTo(checker)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(checker, is(not(equalTo(null))));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(checker.hashCode(), is(checker.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(checker.toString(), startsWith("NullDeprecatedEntityChecker"));
    }

    @Test
    public void shouldReturnFalse() {
        assertThat(checker.isDeprecated(mock(OWLEntity.class)), is(false));
    }
}
