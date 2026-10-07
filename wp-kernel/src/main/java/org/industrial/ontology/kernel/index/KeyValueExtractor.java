package org.industrial.ontology.kernel.index;




import org.semanticweb.owlapi.model.OWLAxiom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.KeyValueExtractor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-07
 */
@FunctionalInterface
public interface KeyValueExtractor<V, A extends OWLAxiom> {

    @Nullable
    V extractValue(@Nonnull A axiom);
}
