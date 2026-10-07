package org.industrial.ontology.kernel.change;



import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.chg.OntologyChangeTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-28
 */
public class OntologyChangeTranslator {

    @Nonnull
    private final OntologyChangeTranslatorVisitor visitor;

    public OntologyChangeTranslator(@Nonnull OntologyChangeTranslatorVisitor visitor) {
        this.visitor = checkNotNull(visitor);
    }

    @Nonnull
    public OWLOntologyChange toOwlOntologyChange(@Nonnull OntologyChange change) {
        return change.accept(visitor);
    }
}
