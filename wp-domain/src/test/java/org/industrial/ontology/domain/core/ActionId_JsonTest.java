package org.industrial.ontology.domain.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.access.ActionId_Json_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2 Feb 2018
 */
public class ActionId_JsonTest {

    private static final String ID = "TheActionId";

    private ActionId actionId;

    @BeforeEach
    public void setUp() throws Exception {
        actionId = new ActionId(ID);
    }

    @Test
    public void shouldSerializeJson() throws Exception {
        String result = new ObjectMapper().writeValueAsString(actionId);
        assertThat(result, is("\"" + ID + "\""));
    }

    @Test
    public void shouldDeserializeJson() throws Exception {
        ActionId readActionId = new ObjectMapper().readerFor(ActionId.class).readValue("\"" + ID + "\"");
        assertThat(readActionId, is(actionId));
    }
}
