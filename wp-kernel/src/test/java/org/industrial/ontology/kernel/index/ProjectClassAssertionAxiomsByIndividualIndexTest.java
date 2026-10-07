package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ProjectClassAssertionAxiomsByIndividualIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectClassAssertionAxiomsByIndividualIndexTest {

    private ProjectClassAssertionAxiomsByIndividualIndex impl;

    @Mock
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Mock
    private ClassAssertionAxiomsByIndividualIndex classAssertionAxiomsIndex;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLClassAssertionAxiom classAssertionAxiom;

    @Mock
    private OWLIndividual individual;

    @BeforeEach
    public void setUp() {
        impl = new ProjectClassAssertionAxiomsByIndividualIndex(projectOntologiesIndex, classAssertionAxiomsIndex);
        when(projectOntologiesIndex.getOntologyIds()).thenReturn(Stream.of(ontologyId));
        when(classAssertionAxiomsIndex.getClassAssertionAxioms(any(), any())).thenReturn(Stream.empty());
        when(classAssertionAxiomsIndex.getClassAssertionAxioms(individual, ontologyId)).thenReturn(Stream.of(classAssertionAxiom));
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), contains(projectOntologiesIndex, classAssertionAxiomsIndex));
    }

    @Test
    public void shouldGetClassAssertionAxioms() {
        var axioms = impl.getClassAssertionAxioms(individual).collect(toSet());
        assertThat(axioms, Matchers.hasItem(classAssertionAxiom));
    }

    @Test
    public void shouldNotGetAxiomsForUnknowIndividual() {
        var axioms = impl.getClassAssertionAxioms(mock(OWLIndividual.class)).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfIndividualIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.getClassAssertionAxioms(null);
        });
    }
}
