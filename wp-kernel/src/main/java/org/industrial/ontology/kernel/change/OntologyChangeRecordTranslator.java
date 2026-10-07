package org.industrial.ontology.kernel.change;



import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;

import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.change.OntologyChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OntologyChangeRecordTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-16
 */
public interface OntologyChangeRecordTranslator {

    @Nonnull
    OntologyChange getOntologyChange(@Nonnull OWLOntologyChangeRecord record);
}
