package org.industrial.ontology.kernel.api.index;



import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.AxiomsByEntityReferenceIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-07
 */
public interface AxiomsByEntityReferenceIndex extends Index {

    Stream<OWLAxiom> getReferencingAxioms(@Nonnull
                                          OWLEntity entity,
                                          @Nonnull
                                          OWLOntologyID ontologyId);
}
