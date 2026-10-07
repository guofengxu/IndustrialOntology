package org.industrial.ontology.domain.renderer;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.renderer.HasHtmlBrowserText}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public interface HasHtmlBrowserText {

    String getHtmlBrowserText(OWLObject object);
}
