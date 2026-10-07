package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.EntitiesInOntologySignatureIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
public interface EntitiesInOntologySignatureIndex extends Index {

    boolean containsEntityInSignature(@Nonnull OWLEntity entity,
                                      @Nonnull OWLOntologyID ontologyId);
}
