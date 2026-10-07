package org.industrial.ontology.kernel.hierarchy;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLDataPropertyProvider;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.DataPropertyHierarchyProvider_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataPropertyHierarchyProviderTest {

    private DataPropertyHierarchyProvider provider;

    @Mock
    private ProjectId projectId;

    @Mock
    private OWLDataPropertyProvider dataPropertyProvider;

    @Mock
    private ProjectSignatureByTypeIndex projectSignatureIndex;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private SubDataPropertyAxiomsBySubPropertyIndex subPropertyAxiomsIndex;

    @Mock
    private EntitiesInProjectSignatureIndex entitiesInSignature;

    @Mock
    private OWLOntologyID ontologyId;

    private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private OWLDataProperty propertyA = dataFactory.getOWLDataProperty(mockIri());

    private OWLDataProperty propertyB = dataFactory.getOWLDataProperty(mockIri());

    private OWLDataProperty propertyC = dataFactory.getOWLDataProperty(mockIri());

    private OWLDataProperty propertyD = dataFactory.getOWLDataProperty(mockIri());

    @Mock
    private AxiomsByTypeIndex axiomsByTypeIndex;

    @Mock
    private OntologySignatureByTypeIndex ontologySignatureByTypeIndex;

    private static IRI mockIri() {
        return mock(IRI.class);
    }

    @BeforeEach
    public void setUp() {
        when(projectOntologiesIndex.getOntologyIds()).thenAnswer(invocation -> Stream.of(ontologyId));
        var propertyASubPropertyOfB = dataFactory.getOWLSubDataPropertyOfAxiom(propertyA, propertyB, noAnnotations());
        var propertyBSubPropertyOfC = dataFactory.getOWLSubDataPropertyOfAxiom(propertyB, propertyC, noAnnotations());
        when(subPropertyAxiomsIndex.getSubPropertyOfAxioms(any(), any())).thenAnswer(invocation -> Stream.empty());
        when(subPropertyAxiomsIndex.getSubPropertyOfAxioms(propertyA, ontologyId)).thenAnswer(invocation -> Stream.of(propertyASubPropertyOfB));
        when(subPropertyAxiomsIndex.getSubPropertyOfAxioms(propertyB, ontologyId)).thenAnswer(invocation -> Stream.of(propertyBSubPropertyOfC));
        when(entitiesInSignature.containsEntityInSignature(propertyC)).thenReturn(true);
        when(entitiesInSignature.containsEntityInSignature(propertyD)).thenReturn(true);
        when(ontologySignatureByTypeIndex.getSignature(EntityType.DATA_PROPERTY, ontologyId)).thenAnswer(invocation -> Stream.of(propertyA, propertyB, propertyC, propertyD));
        when(axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_DATA_PROPERTY, ontologyId)).thenAnswer(invocation -> Stream.of(propertyASubPropertyOfB, propertyBSubPropertyOfC));
        provider = new DataPropertyHierarchyProvider(projectId, dataFactory.getOWLTopDataProperty(), projectOntologiesIndex, axiomsByTypeIndex, ontologySignatureByTypeIndex, subPropertyAxiomsIndex, entitiesInSignature);
    }

    private static Set<OWLAnnotation> noAnnotations() {
        return Collections.emptySet();
    }

    @Test
    public void shouldGetParents() {
        var parents = provider.getParents(propertyA);
        assertThat(parents, contains(propertyB));
    }

    @Test
    public void shouldGetAncestors() {
        var ancestors = provider.getAncestors(propertyA);
        assertThat(ancestors, containsInAnyOrder(propertyB, propertyC));
    }

    @Test
    public void shouldGetChidlren() {
        var children = provider.getChildren(propertyC);
        assertThat(children, contains(propertyB));
    }

    @Test
    public void shouldGetDescendants() {
        var descendants = provider.getDescendants(propertyC);
        assertThat(descendants, containsInAnyOrder(propertyA, propertyB));
    }

    @Test
    public void shouldGetRoots() {
        var roots = provider.getRoots();
        assertThat(roots, hasItems(dataFactory.getOWLTopDataProperty()));
    }
}
