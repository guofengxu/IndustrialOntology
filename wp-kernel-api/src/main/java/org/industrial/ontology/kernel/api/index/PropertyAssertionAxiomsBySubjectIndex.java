package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.PropertyAssertionAxiomsBySubjectIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-12
 */
public interface PropertyAssertionAxiomsBySubjectIndex extends Index {

    @Nonnull
    Stream<OWLAxiom> getPropertyAssertions(@Nonnull
                                                                                                                                   OWLIndividual individual,
                                                                                                                                   @Nonnull
                                                                                                                                   OWLOntologyID ontologyId);
}
