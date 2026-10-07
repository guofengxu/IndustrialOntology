package org.industrial.ontology.kernel.change;



import org.industrial.ontology.domain.event.ProjectEvent;

import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.HasHighLevelEvents}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 12/09/2013
 */
public interface HasHighLevelEvents {

    List<ProjectEvent> getHighLevelEvents();
}
