package org.industrial.ontology.kernel.change;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ChangeDescriptionGenerator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 25/02/2013
 */
public interface ChangeDescriptionGenerator<S> {

    String generateChangeDescription(ChangeApplicationResult<S> result);
}
