package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import java.util.Collections;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.IndividualsByTypeIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IndividualsByTypeIndexTest {

    private IndividualsByTypeIndex impl;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private ProjectSignatureByTypeIndex projectSignatureIndex;

    @Mock
    private ClassAssertionAxiomsByIndividualIndex classAssertionsByIndividual;

    @Mock
    private ClassAssertionAxiomsByClassIndex classAssertionsByClass;

    @Mock
    private ClassHierarchyProvider classHierarchyProvider;

    @Mock
    private DictionaryManager dictionaryManager;

    @Mock
    private OWLDataFactory dataFactory;

    private OWLClass owlThing = new OWLClassImpl(OWLRDFVocabulary.OWL_THING.getIRI());

    @Mock
    private OWLOntologyID ontologyId;

    private OWLNamedIndividual indA = new OWLNamedIndividualImpl(mock(IRI.class)), indB = new OWLNamedIndividualImpl(mock(IRI.class));

    @Mock
    private OWLClassAssertionAxiom indATypeClsA;

    private OWLClass clsA = new OWLClassImpl(mock(IRI.class)), clsB = new OWLClassImpl(mock(IRI.class));

    @BeforeEach
    public void setUp() {
        impl = new IndividualsByTypeIndex(projectOntologiesIndex, projectSignatureIndex, classAssertionsByIndividual, classAssertionsByClass, classHierarchyProvider, dictionaryManager, dataFactory);
        // SubClassOf(:clsA, :clsB)
        // ClassAssertion(:clsA :indA)
        when(dataFactory.getOWLThing()).thenReturn(owlThing);
        when(projectOntologiesIndex.getOntologyIds()).thenAnswer(invocation -> Stream.of(ontologyId));
        when(projectSignatureIndex.getSignature(EntityType.NAMED_INDIVIDUAL)).thenAnswer(invocation -> Stream.of(indA, indB));
        when(indATypeClsA.getClassExpression()).thenReturn(clsA);
        when(indATypeClsA.getIndividual()).thenReturn(indA);
        when(classAssertionsByIndividual.getClassAssertionAxioms(any(), any())).thenAnswer(invocation -> Stream.empty());
        when(classAssertionsByIndividual.getClassAssertionAxioms(indA, ontologyId)).thenAnswer(invocation -> Stream.of(indATypeClsA));
        when(classAssertionsByClass.getClassAssertionAxioms(any(), any())).thenAnswer(invocation -> Stream.empty());
        when(classAssertionsByClass.getClassAssertionAxioms(clsA, ontologyId)).thenAnswer(invocation -> Stream.of(indATypeClsA));
        when(classHierarchyProvider.getDescendants(clsB)).thenAnswer(invocation -> Collections.singleton(clsA));
        when(dictionaryManager.getShortForm(indA)).thenReturn("indA");
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), containsInAnyOrder(projectOntologiesIndex, projectSignatureIndex, classAssertionsByIndividual, classAssertionsByClass));
    }

    @Test
    public void shouldGetUntypedIndividualsAsDirectInstancesOfOwlThing() {
        var inds = impl.getIndividualsByType(owlThing, InstanceRetrievalMode.DIRECT_INSTANCES).collect(toSet());
        assertThat(inds, contains(indB));
    }

    @Test
    public void shouldAllIndividualsAsIndirectInstancesOfOwlThing() {
        var inds = impl.getIndividualsByType(owlThing, InstanceRetrievalMode.ALL_INSTANCES).collect(toSet());
        assertThat(inds, containsInAnyOrder(indA, indB));
    }

    @Test
    public void shouldGetDirectAssertedInstancesOfClsA() {
        var inds = impl.getIndividualsByType(clsA, InstanceRetrievalMode.DIRECT_INSTANCES).collect(toSet());
        assertThat(inds, containsInAnyOrder(indA));
    }

    @Test
    public void shouldGetInirectAssertedInstancesOfClsA() {
        var inds = impl.getIndividualsByType(clsA, InstanceRetrievalMode.ALL_INSTANCES).collect(toSet());
        assertThat(inds, containsInAnyOrder(indA));
    }

    @Test
    public void shouldGetDirectAssertedInstancesOfClsB() {
        var inds = impl.getIndividualsByType(clsB, InstanceRetrievalMode.DIRECT_INSTANCES).collect(toSet());
        assertThat(inds.isEmpty(), is(true));
    }

    @Test
    public void shouldGetIndirectAssertedInstancesOfClsB() {
        var inds = impl.getIndividualsByType(clsB, InstanceRetrievalMode.ALL_INSTANCES).collect(toSet());
        assertThat(inds, contains(indA));
    }
}
