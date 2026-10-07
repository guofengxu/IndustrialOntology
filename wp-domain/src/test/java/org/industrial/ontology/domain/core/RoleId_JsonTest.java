package org.industrial.ontology.domain.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.access.RoleId_Json_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2 Feb 2018
 */
public class RoleId_JsonTest {

    private static final String ID = "TheRoleId";

    private RoleId roleId;

    @BeforeEach
    public void setUp() throws Exception {
        roleId = new RoleId(ID);
    }

    @Test
    public void shouldSerializeJson() throws Exception {
        String result = new ObjectMapper().writeValueAsString(roleId);
        assertThat(result, is("\"" + ID + "\""));
    }

    @Test
    public void shouldDeserializeJson() throws Exception {
        RoleId readRoleId = new ObjectMapper().readerFor(RoleId.class).readValue("\"" + ID + "\"");
        assertThat(readRoleId, is(roleId));
    }
}
