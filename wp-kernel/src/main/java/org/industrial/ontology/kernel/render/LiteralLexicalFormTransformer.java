package org.industrial.ontology.kernel.render;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.LiteralLexicalFormTransformer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-02
 */
public interface LiteralLexicalFormTransformer {

    @Nonnull
    String transformLexicalForm(@Nonnull String lexicalForm);
}
