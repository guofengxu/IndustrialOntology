package org.industrial.ontology.kernel.util;

import org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Collections;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.ReferenceFinder_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-08
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReferenceFinderTest {

    private ReferenceFinder referenceFinder;

    @Mock
    private AxiomsByReferenceIndex axiomsByReferenceIndex;

    @Mock
    private OntologyAnnotationsIndex ontologyAnnotationsIndex;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI entityIri;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLAnnotation annotation;

    @Mock
    private OWLAnnotationProperty annotationProperty;

    @Mock
    private OWLAnnotation annotationAnnotation;

    @Mock
    private OWLEntity annotationAnnotationValueEntity;

    @Mock
    private IRI annotationAnnotationValueIri;

    @Mock
    private OWLAnnotationProperty annoationAnnotationProperty;

    @BeforeEach
    public void setUp() {
        when(entity.getIRI()).thenReturn(entityIri);
        // Return nothing by default
        when(axiomsByReferenceIndex.getReferencingAxioms(any(), any())).thenReturn(Stream.empty());
        when(axiomsByReferenceIndex.getReferencingAxioms(Collections.singleton(entity), ontologyId)).thenReturn(Stream.of(axiom));
        when(annotation.getValue()).thenReturn(entityIri);
        when(annotation.getProperty()).thenReturn(annotationProperty);
        when(annotation.getAnnotations()).thenReturn(Collections.singleton(annotationAnnotation));
        when(annotationAnnotation.getProperty()).thenReturn(annoationAnnotationProperty);
        when(annotationAnnotationValueEntity.getIRI()).thenReturn(annotationAnnotationValueIri);
        when(annotationAnnotation.getValue()).thenReturn(annotationAnnotationValueIri);
        // Return nothing by default
        when(ontologyAnnotationsIndex.getOntologyAnnotations(any())).thenReturn(Stream.empty());
        when(ontologyAnnotationsIndex.getOntologyAnnotations(ontologyId)).thenReturn(Stream.of(annotation));
        referenceFinder = new ReferenceFinder(axiomsByReferenceIndex, ontologyAnnotationsIndex);
    }

    @Test
    public void shouldReferenceOntologyId() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(entity), ontologyId);
        assertThat(referenceSet.getOntologyId(), is(ontologyId));
    }

    @Test
    public void shouldRetrieveAxiomReferences() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(entity), ontologyId);
        assertThat(referenceSet.getReferencingAxioms(), hasItem(axiom));
    }

    @Test
    public void shouldRetrieveOntologyAnnotationReferencesByAnnotationValue() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(entity), ontologyId);
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(annotation));
    }

    @Test
    public void shouldRetrieveOntologyAnnotationReferencesByAnnotationProperty() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(annotationProperty), ontologyId);
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(annotation));
    }

    @Test
    public void shouldRetrieveAnnotationsByNestedAnnotationValue() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(annotationAnnotationValueEntity), ontologyId);
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(annotation));
    }

    @Test
    public void shouldRetrieveAnnotationsByNestedAnnotationProperty() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.singleton(annoationAnnotationProperty), ontologyId);
        assertThat(referenceSet.getReferencingOntologyAnnotations(), hasItem(annotation));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfEntitySetIsNull() {
        assertThrows(NullPointerException.class, () -> {
            referenceFinder.getReferenceSet(null, ontologyId);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfOntologyIdIsNull() {
        assertThrows(NullPointerException.class, () -> {
            referenceFinder.getReferenceSet(Collections.singleton(entity), null);
        });
    }

    @Test
    public void shouldReturnEmptyResultForEmptyEntitiesSet() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.emptySet(), ontologyId);
        assertThatReferenceSetIsEmpty(referenceSet);
    }

    @Test
    public void shouldReturnEmptyResultForUnknownOntologyId() {
        var referenceSet = referenceFinder.getReferenceSet(Collections.emptySet(), mock(OWLOntologyID.class));
        assertThatReferenceSetIsEmpty(referenceSet);
    }

    private static void assertThatReferenceSetIsEmpty(ReferenceFinder.ReferenceSet referenceSet) {
        assertThat(referenceSet.getReferencingOntologyAnnotations().isEmpty(), is(true));
        assertThat(referenceSet.getReferencingAxioms().isEmpty(), is(true));
    }
}
