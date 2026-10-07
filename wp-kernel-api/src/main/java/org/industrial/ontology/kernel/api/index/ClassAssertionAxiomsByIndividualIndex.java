package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ClassAssertionAxiomsByIndividualIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public interface ClassAssertionAxiomsByIndividualIndex extends Index {

    Stream<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull
                                                           OWLIndividual individual,
                                                           @Nonnull
                                                           OWLOntologyID ontologyID);
}
