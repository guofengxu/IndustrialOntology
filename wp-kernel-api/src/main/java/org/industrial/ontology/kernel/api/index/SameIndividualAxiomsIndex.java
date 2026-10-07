package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.SameIndividualAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public interface SameIndividualAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLSameIndividualAxiom> getSameIndividualAxioms(@Nonnull
                                                           OWLIndividual individual,
                                                           @Nonnull
                                                           OWLOntologyID ontologyId);
}
