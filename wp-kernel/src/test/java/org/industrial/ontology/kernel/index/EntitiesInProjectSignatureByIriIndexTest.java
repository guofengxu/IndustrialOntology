package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.IRI;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EntitiesInProjectSignatureByIriIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-13
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntitiesInProjectSignatureByIriIndexTest {

    private EntitiesInProjectSignatureByIriIndex impl;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private IRI iri;

    @Mock
    private OWLOntologyID ontAId, ontBId;

    @Mock
    private OWLClass entityCls;

    @Mock
    private OWLNamedIndividual entityIndividual;

    @Mock
    private EntitiesInOntologySignatureByIriIndex entitiesInOntologySignatureByIriIndex;

    @BeforeEach
    public void setUp() {
        when(projectOntologiesIndex.getOntologyIds()).thenReturn(Stream.of(ontAId, ontBId));
        when(entitiesInOntologySignatureByIriIndex.getEntitiesInSignature(iri, ontAId)).thenAnswer(inv -> Stream.of(entityCls, entityIndividual));
        when(entitiesInOntologySignatureByIriIndex.getEntitiesInSignature(iri, ontBId)).thenAnswer(inv -> Stream.of(entityCls));
        impl = new EntitiesInProjectSignatureByIriIndex(projectOntologiesIndex, entitiesInOntologySignatureByIriIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), containsInAnyOrder(projectOntologiesIndex, entitiesInOntologySignatureByIriIndex));
    }

    @Test
    public void shouldReturnDistinctEntitiesInSignature() {
        var entities = impl.getEntitiesInSignature(iri).collect(Collectors.toList());
        assertThat(entities, Matchers.containsInAnyOrder(entityCls, entityIndividual));
    }

    @Test
    public void shouldReturnEmptyStreamForUnknownIri() {
        var entities = impl.getEntitiesInSignature(mock(IRI.class)).collect(toSet());
        assertThat(entities.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfIriIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.getEntitiesInSignature(null);
        });
    }
}
