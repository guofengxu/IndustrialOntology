package org.industrial.ontology.kernel.owlapi;



import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.ProjectAnnotationAssertionAxiomsBySubjectIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/01/15
 */
public class ProjectAnnotationAssertionAxiomsBySubjectIndex implements org.industrial.ontology.kernel.api.index.ProjectAnnotationAssertionAxiomsBySubjectIndex {

    @Nonnull
    private final ProjectOntologiesIndex ontologiesIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    public ProjectAnnotationAssertionAxiomsBySubjectIndex(@Nonnull ProjectOntologiesIndex ontologiesIndex,
                                                              @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex) {
        this.ontologiesIndex = checkNotNull(ontologiesIndex);
        this.annotationAssertionsIndex = checkNotNull(annotationAssertionsIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull OWLAnnotationSubject subject) {
        checkNotNull(subject);
        return ontologiesIndex.getOntologyIds()
                .flatMap(ontId -> annotationAssertionsIndex.getAxiomsForSubject(subject, ontId));
    }
}
