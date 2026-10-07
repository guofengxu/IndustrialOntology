package org.industrial.ontology.kernel.change;



import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Set;
import org.industrial.ontology.kernel.api.change.OntologyChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.HasGetChangeSubjects}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
public interface HasGetChangeSubjects {

    Set<OWLEntity> getChangeSubjects(OntologyChange change);
}
