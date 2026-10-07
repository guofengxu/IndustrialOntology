package org.industrial.ontology.kernel.api.index;



import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ProjectAnnotationAssertionAxiomsBySubjectIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/01/15
 */
public interface ProjectAnnotationAssertionAxiomsBySubjectIndex {

    /**
     * Gets the {@link OWLAnnotationAssertionAxiom}s that are contained in project
     * ontologies and have the specified subject.
     * @param subject The subject
     */
    @Nonnull
    Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull OWLAnnotationSubject subject);
}
