package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.AnnotationAxiomsByIriReferenceIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByEntityReferenceIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Collections;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AxiomsByReferenceIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AxiomsByReferenceIndexTest {

    private AxiomsByReferenceIndex impl;

    @Mock
    private AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex;

    @Mock
    private AnnotationAxiomsByIriReferenceIndex axiomsByIriReferenceIndex;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI entityIri;

    @Mock
    private OWLAxiom entityRefAxiom;

    @Mock
    private OWLAnnotationAxiom iriRefAxiom;

    @BeforeEach
    public void setUp() {
        when(entity.getIRI()).thenReturn(entityIri);
        when(axiomsByEntityReferenceIndex.getReferencingAxioms(any(), any())).thenReturn(Stream.empty());
        when(axiomsByEntityReferenceIndex.getReferencingAxioms(entity, ontologyId)).thenReturn(Stream.of(entityRefAxiom));
        when(axiomsByIriReferenceIndex.getReferencingAxioms(any(), any())).thenReturn(Stream.empty());
        when(axiomsByIriReferenceIndex.getReferencingAxioms(entityIri, ontologyId)).thenReturn(Stream.of(iriRefAxiom));
        impl = new AxiomsByReferenceIndex(axiomsByEntityReferenceIndex, axiomsByIriReferenceIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), containsInAnyOrder(axiomsByEntityReferenceIndex, axiomsByIriReferenceIndex));
    }

    @Test
    public void shouldGetAxiomsByReference() {
        var referencingAxiomsStream = impl.getReferencingAxioms(Collections.singleton(entity), ontologyId);
        var referencingAxioms = referencingAxiomsStream.collect(toSet());
        assertThat(referencingAxioms, hasItems(entityRefAxiom, iriRefAxiom));
    }

    @Test
    public void shouldGetEmptyStreamForUnknownOntologyId() {
        var referencingAxiomsStream = impl.getReferencingAxioms(Collections.singleton(entity), mock(OWLOntologyID.class));
        var axiomsCount = referencingAxiomsStream.count();
        assertThat(axiomsCount, is(0L));
    }

    @Test
    public void shouldGetEmptyStreamForEmptyEntitiesSet() {
        var referencingAxiomsStream = impl.getReferencingAxioms(Collections.emptySet(), ontologyId);
        var axiomsCount = referencingAxiomsStream.count();
        assertThat(axiomsCount, is(0L));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionForNullEntitiesSet() {
        assertThrows(NullPointerException.class, () -> {
            impl.getReferencingAxioms(null, ontologyId);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionForNullOntologyId() {
        assertThrows(NullPointerException.class, () -> {
            impl.getReferencingAxioms(Collections.singleton(entity), null);
        });
    }
}
