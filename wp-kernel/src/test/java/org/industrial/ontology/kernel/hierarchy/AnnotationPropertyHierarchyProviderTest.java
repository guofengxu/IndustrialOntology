package org.industrial.ontology.kernel.hierarchy;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubAnnotationPropertyOfAxiomImpl;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.createAnnotationProperty;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySuperPropertyIndex;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyProvider;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotation;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.AnnotationPropertyHierarchyProviderImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-16
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnnotationPropertyHierarchyProviderTest {

    private AnnotationPropertyHierarchyProvider provider;

    @Mock
    private ProjectId projectId;

    @Mock
    private OWLAnnotationPropertyProvider annotationPropertyProvider;

    @Mock
    private ProjectSignatureByTypeIndex projectSignatureIndex;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private SubAnnotationPropertyAxiomsBySubPropertyIndex subPropertyAxiomsIndex;

    @Mock
    private EntitiesInProjectSignatureIndex entitiesInSignature;

    private OWLAnnotationProperty propertyA = createAnnotationProperty(), propertyB = createAnnotationProperty(), propertyC = createAnnotationProperty(), propertyD = createAnnotationProperty();

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private SubAnnotationPropertyAxiomsBySuperPropertyIndex subPropertyAxiomsBySuperPropertyIndex;

    @BeforeEach
    public void setUp() {
        when(annotationPropertyProvider.getOWLAnnotationProperty(any())).then(invocation -> new OWLAnnotationPropertyImpl((IRI) invocation.getArguments()[0]));
        when(projectSignatureIndex.getSignature(EntityType.ANNOTATION_PROPERTY)).thenAnswer(invocation -> Stream.of(propertyA, propertyB, propertyC, propertyD));
        when(projectOntologiesIndex.getOntologyIds()).thenAnswer(invocation -> Stream.of(ontologyId));
        var propertyASubPropertyOfB = new OWLSubAnnotationPropertyOfAxiomImpl(propertyA, propertyB, noAnnotations());
        var propertyBSubPropertyOfC = new OWLSubAnnotationPropertyOfAxiomImpl(propertyB, propertyC, noAnnotations());
        when(subPropertyAxiomsIndex.getSubPropertyOfAxioms(propertyA, ontologyId)).thenAnswer(invocation -> Stream.of(propertyASubPropertyOfB));
        when(subPropertyAxiomsIndex.getSubPropertyOfAxioms(propertyB, ontologyId)).thenAnswer(invocation -> Stream.of(propertyBSubPropertyOfC));
        when(subPropertyAxiomsBySuperPropertyIndex.getAxiomsForSuperProperty(propertyC, ontologyId)).thenAnswer(invocation -> Stream.of(propertyBSubPropertyOfC));
        when(subPropertyAxiomsBySuperPropertyIndex.getAxiomsForSuperProperty(propertyB, ontologyId)).thenAnswer(invocation -> Stream.of(propertyASubPropertyOfB));
        when(entitiesInSignature.containsEntityInSignature(propertyC)).thenReturn(true);
        when(entitiesInSignature.containsEntityInSignature(propertyD)).thenReturn(true);
        provider = new AnnotationPropertyHierarchyProvider(projectId, annotationPropertyProvider, projectSignatureIndex, projectOntologiesIndex, subPropertyAxiomsIndex, subPropertyAxiomsBySuperPropertyIndex, entitiesInSignature);
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
        assertThat(roots, hasItems(propertyC, propertyD));
    }
}
