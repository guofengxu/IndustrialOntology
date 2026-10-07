package org.industrial.ontology.kernel.mansyntax.render;



import org.semanticweb.owlapi.model.OWLObject;

import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.FrameRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public interface FrameRenderer<E extends OWLObject> {

    List<FrameSectionRenderer<E, ?, ?>> getSectionRenderers();
}
