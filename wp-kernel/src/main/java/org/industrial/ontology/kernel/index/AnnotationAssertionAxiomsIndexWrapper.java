package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;

import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ProjectAnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsIndexWrapperImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 17 Jun 2018
 */
public class AnnotationAssertionAxiomsIndexWrapper implements AnnotationAssertionAxiomsIndex, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    @Nonnull
    private final ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    public AnnotationAssertionAxiomsIndexWrapper(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                                     @Nonnull AxiomsByTypeIndex axiomsByTypeIndex,
                                                     @Nonnull ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
        this.annotationAssertionsIndex = checkNotNull(annotationAssertionsIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return Arrays.asList(projectOntologiesIndex, axiomsByTypeIndex);
    }

    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms() {
        return projectOntologiesIndex.getOntologyIds()
                .flatMap(ontId -> axiomsByTypeIndex.getAxiomsByType(AxiomType.ANNOTATION_ASSERTION, ontId));
    }

    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull IRI subject) {
        return annotationAssertionsIndex.getAnnotationAssertionAxioms(subject);
    }

    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty property) {
        return annotationAssertionsIndex.getAnnotationAssertionAxioms(subject)
                           .filter(ax -> ax.getProperty().equals(property));
    }

    @Override
    public long getAnnotationAssertionAxiomsCount(@Nonnull IRI subject) {
        return getAnnotationAssertionAxioms(subject).count();
    }

    @Override
    public long getAnnotationAssertionAxiomsCount(@Nonnull IRI subject, @Nonnull OWLAnnotationProperty property) {
        return getAnnotationAssertionAxioms(subject, property).count();
    }
}
