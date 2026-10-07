package org.industrial.ontology.kernel.mansyntax.render;



import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.HasGetRendering}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22 Apr 2017
 */
public interface HasGetRendering {

    OWLEntityData getRendering(OWLEntity entity);
}
