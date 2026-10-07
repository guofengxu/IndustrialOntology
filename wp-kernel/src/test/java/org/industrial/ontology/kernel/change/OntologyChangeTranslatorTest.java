package org.industrial.ontology.kernel.change;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.chg.OntologyChangeTranslator_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyChangeTranslatorTest {

    @Mock
    private OntologyChangeTranslatorVisitor visitor;

    private OntologyChangeTranslator translator;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLOntologyChange ontologyChange;

    @Mock
    private OWLAnnotation ontologyAnnotation;

    @Mock
    private OWLImportsDeclaration importDeclaration;

    @BeforeEach
    public void setUp() {
        translator = new OntologyChangeTranslator(visitor);
    }

    @Test
    public void shouldTranslateAddAxiomChange() {
        var addAxiomChange = AddAxiomChange.of(ontologyId, axiom);
        when(visitor.visit(addAxiomChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(addAxiomChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }

    @Test
    public void shouldTranslateRemoveAxiomChange() {
        var removeAxiomChange = RemoveAxiomChange.of(ontologyId, axiom);
        when(visitor.visit(removeAxiomChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(removeAxiomChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }

    @Test
    public void shouldTranslateAddOntologyAnnotationChange() {
        var addOntologyAnnotationChange = AddOntologyAnnotationChange.of(ontologyId, ontologyAnnotation);
        when(visitor.visit(addOntologyAnnotationChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(addOntologyAnnotationChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }

    @Test
    public void shouldTranslateRemoveOntologyAnnotationChange() {
        var removeOntologyAnnotationChange = RemoveOntologyAnnotationChange.of(ontologyId, ontologyAnnotation);
        when(visitor.visit(removeOntologyAnnotationChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(removeOntologyAnnotationChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }

    @Test
    public void shouldTranslateAddImportChange() {
        var addImportChange = AddImportChange.of(ontologyId, importDeclaration);
        when(visitor.visit(addImportChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(addImportChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }

    @Test
    public void shouldTranslateRemoveImportChange() {
        var removeImportChange = RemoveImportChange.of(ontologyId, importDeclaration);
        when(visitor.visit(removeImportChange)).thenReturn(ontologyChange);
        var owlOntologyChange = translator.toOwlOntologyChange(removeImportChange);
        assertThat(owlOntologyChange, is(ontologyChange));
    }
}
