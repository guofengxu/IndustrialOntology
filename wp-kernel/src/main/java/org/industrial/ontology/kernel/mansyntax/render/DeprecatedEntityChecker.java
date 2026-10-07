package org.industrial.ontology.kernel.mansyntax.render;



import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.DeprecatedEntityChecker}.
 * <p>
* Matthew Horridge
* Stanford Center for Biomedical Informatics Research
* 27/01/15
*/
public interface DeprecatedEntityChecker {

    boolean isDeprecated(OWLEntity entity);
}
