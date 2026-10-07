package org.industrial.ontology.kernel.change;



import org.semanticweb.owlapi.model.OWLOntologyChange;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.change.OntologyChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OwlOntologyChangeTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public class OwlOntologyChangeTranslator {

    @Nonnull
    private final OwlOntologyChangeTranslatorVisitor visitor;

    public OwlOntologyChangeTranslator(@Nonnull OwlOntologyChangeTranslatorVisitor visitor) {
        this.visitor = checkNotNull(visitor);
    }

    @Nonnull
    public OntologyChange toOntologyChange(@Nonnull OWLOntologyChange change) {
        return change.accept(visitor);
    }
}
