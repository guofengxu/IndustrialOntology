package org.industrial.ontology.domain.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.UserId_Json_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2 Feb 2018
 */
public class UserId_JsonTest {

    private static final String THE_USER_NAME = "The User Name";

    private UserId userId;

    @BeforeEach
    public void setUp() throws Exception {
        userId = UserId.getUserId(THE_USER_NAME);
    }

    @Test
    public void shouldSerializeJson() throws Exception {
        String result = new ObjectMapper().writeValueAsString(userId);
        assertThat(result, is("\"" + THE_USER_NAME + "\""));
    }

    @Test
    public void shouldDeserializeJson() throws Exception {
        UserId readUserId = new ObjectMapper().readerFor(UserId.class).readValue("\"" + THE_USER_NAME + "\"");
        assertThat(readUserId, is(userId));
    }
}
