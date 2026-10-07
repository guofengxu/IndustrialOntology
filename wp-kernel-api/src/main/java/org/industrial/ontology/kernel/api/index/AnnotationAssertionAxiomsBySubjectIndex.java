package org.industrial.ontology.kernel.api.index;



import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.AnnotationAssertionAxiomsBySubjectIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public interface AnnotationAssertionAxiomsBySubjectIndex extends Index {

    Stream<OWLAnnotationAssertionAxiom> getAxiomsForSubject(@Nonnull OWLAnnotationSubject subject,
                                                            @Nonnull OWLOntologyID ontologyId);
}
