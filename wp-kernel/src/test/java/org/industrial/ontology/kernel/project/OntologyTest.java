package org.industrial.ontology.kernel.project;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.Ontology_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyTest {

    private Ontology ontology;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLAnnotation annotation;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLImportsDeclaration importsDeclaration;

    @BeforeEach
    public void setUp() {
        ontology = Ontology.get(ontologyId, ImmutableSet.of(importsDeclaration), ImmutableSet.of(annotation), ImmutableSet.of(axiom));
    }

    @Test
    public void shouldGetSuppliedOntologyId() {
        assertThat(ontology.getOntologyId(), is(ontologyId));
    }

    @Test
    public void shouldGetSuppliedImportsDeclaration() {
        assertThat(ontology.getImportsDeclarations(), contains(importsDeclaration));
    }

    @Test
    public void shouldGetSuppliedAnnotations() {
        assertThat(ontology.getAnnotations(), contains(annotation));
    }

    @Test
    public void shouldGetSuppliedAxioms() {
        assertThat(ontology.getAxioms(), contains(axiom));
    }
}
