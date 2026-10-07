package org.industrial.ontology.kernel.mansyntax.render;



import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.HttpLinkRenderer}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 21/02/2014
 */
public interface HttpLinkRenderer {

    boolean isLink(OWLLiteral literal);

    void renderLink(String link, StringBuilder builder);

}
