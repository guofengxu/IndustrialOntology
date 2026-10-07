package org.industrial.ontology.kernel.index;



import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;

import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ObjectPropertyAssertionAxiomsBySubjectIndex;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.PropertyAssertionAxiomsBySubjectIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-12
 */
public class PropertyAssertionAxiomsBySubjectIndex implements org.industrial.ontology.kernel.api.index.PropertyAssertionAxiomsBySubjectIndex, DependentIndex {

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubject;

    @Nonnull
    private final ObjectPropertyAssertionAxiomsBySubjectIndex objectPropertyAssertionAxiomsBySubject;

    @Nonnull
    private final DataPropertyAssertionAxiomsBySubjectIndex dataPropertyAssertionAxiomsBySubject;

    public PropertyAssertionAxiomsBySubjectIndex(@Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubject,
                                                     @Nonnull ObjectPropertyAssertionAxiomsBySubjectIndex objectPropertyAssertionAxiomsBySubject,
                                                     @Nonnull DataPropertyAssertionAxiomsBySubjectIndex dataPropertyAssertionAxiomsBySubject) {
        this.annotationAssertionAxiomsBySubject = checkNotNull(annotationAssertionAxiomsBySubject);
        this.objectPropertyAssertionAxiomsBySubject = checkNotNull(objectPropertyAssertionAxiomsBySubject);
        this.dataPropertyAssertionAxiomsBySubject = checkNotNull(dataPropertyAssertionAxiomsBySubject);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(annotationAssertionAxiomsBySubject,
                       objectPropertyAssertionAxiomsBySubject,
                       dataPropertyAssertionAxiomsBySubject);
    }

    @Nonnull
    @Override
    public Stream<OWLAxiom> getPropertyAssertions(@Nonnull OWLIndividual subject,
                                                  @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(subject);
        checkNotNull(ontologyId);
        var annotationAssertions = getAnnotationAssertionAxioms(subject, ontologyId);
        var objectPropertyAssertions = objectPropertyAssertionAxiomsBySubject.getObjectPropertyAssertions(subject, ontologyId);
        var dataPropertyAssertions = dataPropertyAssertionAxiomsBySubject.getDataPropertyAssertions(subject, ontologyId);
        return Stream
                .of(annotationAssertions,
                    dataPropertyAssertions,
                    objectPropertyAssertions)
                .flatMap(ax -> ax);
    }

    private Stream<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull OWLIndividual subject,
                                                                             @Nonnull OWLOntologyID ontologyId) {
        OWLAnnotationSubject annotationSubject;
        if(subject instanceof OWLNamedIndividual) {
            annotationSubject = ((OWLNamedIndividual) subject).getIRI();
        }
        else {
            annotationSubject = (OWLAnonymousIndividual) subject;
        }
        return annotationAssertionAxiomsBySubject.getAxiomsForSubject(annotationSubject, ontologyId);
    }
}
