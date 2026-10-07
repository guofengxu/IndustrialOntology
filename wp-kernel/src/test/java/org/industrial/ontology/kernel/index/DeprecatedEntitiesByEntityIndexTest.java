package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DeprecatedEntitiesByEntityIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeprecatedEntitiesByEntityIndexTest {

    private DeprecatedEntitiesByEntityIndex impl;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI entityIri;

    @Mock
    private OWLAnnotationAssertionAxiom annotationAssertion;

    @BeforeEach
    public void setUp() {
        impl = new DeprecatedEntitiesByEntityIndex(projectOntologiesIndex, annotationAssertionsIndex);
        when(projectOntologiesIndex.getOntologyIds()).thenReturn(Stream.of(ontologyId));
        when(annotationAssertionsIndex.getAxiomsForSubject(any(), any())).thenReturn(Stream.empty());
        when(annotationAssertionsIndex.getAxiomsForSubject(entityIri, ontologyId)).thenReturn(Stream.of(annotationAssertion));
        when(entity.getIRI()).thenReturn(entityIri);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), containsInAnyOrder(projectOntologiesIndex, annotationAssertionsIndex));
    }

    @Test
    public void shouldNotFindEntityToBeDeprecated() {
        var deprecated = impl.isDeprecated(entity);
        assertThat(deprecated, Matchers.is(false));
    }

    @Test
    public void shouldFindEntityToBeDeprecated() {
        when(annotationAssertion.isDeprecatedIRIAssertion()).thenReturn(true);
        var deprecated = impl.isDeprecated(entity);
        assertThat(deprecated, Matchers.is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfEntityIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.isDeprecated(null);
        });
    }
}
