package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ObjectPropertyAssertionAxiomsBySubjectIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-11
 */
public interface ObjectPropertyAssertionAxiomsBySubjectIndex extends Index {

    /**
     * Gets the {@link OWLObjectPropertyAssertionAxiom}s that have the specified individual as
     * a subject.
     * @param subject The subject.
     * @param ontologyId The id of the ontology to examine.
     */
    @Nonnull
    Stream<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertions(@Nonnull
                                                                        OWLIndividual subject,
                                                                        @Nonnull
                                                                        OWLOntologyID ontologyId);
}
