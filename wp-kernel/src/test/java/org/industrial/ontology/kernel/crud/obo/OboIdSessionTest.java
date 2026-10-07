package org.industrial.ontology.kernel.crud.obo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OboIdSessionTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 15/09/2014
 */
public class OboIdSessionTest {

    private OBOIdSession session;

    @BeforeEach
    public void setUp() throws Exception {
        session = new OBOIdSession();
    }

    @Test
    public void shouldAddSession() {
        int id = 3000;
        session.addSessionId(id);
        assertThat(session.isSessionId(id), is(true));
    }

    @Test
    public void shouldNotContainSession() {
        assertThat(session.isSessionId(3000), is(false));
    }
}
