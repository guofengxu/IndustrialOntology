package org.industrial.ontology.domain.frame;



import org.industrial.ontology.domain.entity.OWLEntityData;

import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.HasFreshEntities}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 20/03/2014
 */
public interface HasFreshEntities {

    Set<OWLEntityData> getFreshEntities();
}
