package org.industrial.ontology.kernel.crud.obo;



import com.google.common.collect.Sets;
import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;

import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OBOIdSession}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 15/09/2014
 */
public class OBOIdSession implements ChangeSetEntityCrudSession {

    private Set<Long> sessionIds = Sets.newHashSet();

    public OBOIdSession() {
    }

    public boolean isSessionId(long id) {
        return sessionIds.contains(id);
    }

    public void addSessionId(long id) {
        sessionIds.add(id);
    }
}
