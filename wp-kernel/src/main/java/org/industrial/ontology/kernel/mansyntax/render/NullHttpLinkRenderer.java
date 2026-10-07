package org.industrial.ontology.kernel.mansyntax.render;



import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.NullHttpLinkRenderer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/01/15
 */
public class NullHttpLinkRenderer implements HttpLinkRenderer {

    public NullHttpLinkRenderer() {
    }

    @Override
    public boolean isLink(OWLLiteral literal) {
        return false;
    }

    @Override
    public void renderLink(String link, StringBuilder builder) {
        builder.append(link);
    }
}
