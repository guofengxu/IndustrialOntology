package org.industrial.ontology.kernel.render;



import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.LiteralRenderer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-02
 */
public interface LiteralRenderer {

    @Nonnull
    String getLiteralRendering(@Nonnull
                  OWLLiteral literal);
}
