package org.industrial.ontology.kernel.owlapi;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.ProjectAnnotationAssertionAxiomsBySubjectIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectAnnotationAssertionAxiomsBySubjectIndexTest {

    private ProjectAnnotationAssertionAxiomsBySubjectIndex impl;

    @Mock
    private OWLAnnotationAssertionAxiom annotationAssertionAxiomA, annotationAssertionAxiomB;

    @Mock
    private OWLAnnotationSubject subject;

    @Mock
    private ProjectOntologiesIndex ontologiesIndex;

    @Mock
    private AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    @Mock
    private OWLOntologyID ontologyId;

    @BeforeEach
    public void setUp() throws Exception {
        when(ontologiesIndex.getOntologyIds()).thenReturn(Stream.of(ontologyId));
        when(annotationAssertionsIndex.getAxiomsForSubject(any(), any())).thenReturn(Stream.empty());
        when(annotationAssertionsIndex.getAxiomsForSubject(subject, ontologyId)).thenReturn(Stream.of(annotationAssertionAxiomA, annotationAssertionAxiomB));
        impl = new ProjectAnnotationAssertionAxiomsBySubjectIndex(ontologiesIndex, annotationAssertionsIndex);
    }

    @Test
    public void shouldReturnAssertionsForKnownSubject() {
        Set<OWLAnnotationAssertionAxiom> result = impl.getAnnotationAssertionAxioms(subject).collect(Collectors.toSet());
        assertThat(result, containsInAnyOrder(annotationAssertionAxiomA, annotationAssertionAxiomB));
    }

    @Test
    public void shouldReturnEmptySetForUnknownSubject() {
        Set<OWLAnnotationAssertionAxiom> result = impl.getAnnotationAssertionAxioms(mock(OWLAnnotationSubject.class)).collect(Collectors.toSet());
        assertThat(result.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfSubjectIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.getAnnotationAssertionAxioms(null);
        });
    }
}
