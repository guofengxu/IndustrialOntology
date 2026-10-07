package org.industrial.ontology.kernel.change;

import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ChangeGenerationContext_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChangeGenerationContextTest {

    private ChangeGenerationContext context;

    @Mock
    private UserId userId;

    @BeforeEach
    public void setUp() {
        context = new ChangeGenerationContext(userId);
    }

    @Test
    public void shouldThrowNpeIfUserIdIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ChangeGenerationContext(null);
        });
    }

    @Test
    public void shouldGetUserId() {
        assertThat(context.getUserId(), is(userId));
    }
}
