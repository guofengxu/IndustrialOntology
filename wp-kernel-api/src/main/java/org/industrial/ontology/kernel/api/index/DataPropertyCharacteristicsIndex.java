package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DataPropertyCharacteristicsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public interface DataPropertyCharacteristicsIndex extends Index {

    boolean isFunctional(@Nonnull OWLDataProperty dataProperty,
                         @Nonnull OWLOntologyID ontologyId);
}
