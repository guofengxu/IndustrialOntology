package org.industrial.ontology.kernel.change;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.RemoveAxiom;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OwlOntologyChangeTranslatorVisitor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OwlOntologyChangeTranslatorVisitorTest {

    private OwlOntologyChangeTranslatorVisitor visitor;

    @Mock
    private OWLOntology ontology;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLImportsDeclaration importsDecl;

    @Mock
    private OWLAnnotation ontologyAnnotation;

    @BeforeEach
    public void setUp() {
        when(ontology.getOntologyID()).thenReturn(ontologyId);
        visitor = new OwlOntologyChangeTranslatorVisitor();
    }

    @Test
    public void shouldVisitAddAxiomChange() {
        var addAxiom = new AddAxiom(ontology, axiom);
        var change = addAxiom.accept(visitor);
        assertThat(change, is(instanceOf(AddAxiomChange.class)));
        var addAxiomChange = (AddAxiomChange) change;
        assertThat(addAxiomChange.getOntologyId(), is(ontologyId));
        assertThat(addAxiomChange.getAxiom(), is(axiom));
    }

    @Test
    public void shouldVisitRemoveAxiomChange() {
        var removeAxiom = new RemoveAxiom(ontology, axiom);
        var change = removeAxiom.accept(visitor);
        assertThat(change, is(instanceOf(RemoveAxiomChange.class)));
        var removeAxiomChange = (RemoveAxiomChange) change;
        assertThat(removeAxiomChange.getOntologyId(), is(ontologyId));
        assertThat(removeAxiomChange.getAxiom(), is(axiom));
    }

    @Test
    public void shouldVisitAddOntologyAnnotationChange() {
        var addOntologyAnnotation = new AddOntologyAnnotation(ontology, ontologyAnnotation);
        var change = addOntologyAnnotation.accept(visitor);
        assertThat(change, is(instanceOf(AddOntologyAnnotationChange.class)));
        var addOntologyAnnotationChange = (AddOntologyAnnotationChange) change;
        assertThat(addOntologyAnnotationChange.getOntologyId(), is(ontologyId));
        assertThat(addOntologyAnnotationChange.getAnnotation(), is(ontologyAnnotation));
    }

    @Test
    public void shouldVisitRemoveOntologyAnnotationChange() {
        var removeOntologyAnnotation = new RemoveOntologyAnnotation(ontology, ontologyAnnotation);
        var change = removeOntologyAnnotation.accept(visitor);
        assertThat(change, is(instanceOf(RemoveOntologyAnnotationChange.class)));
        var removeOntologyAnnotationChange = (RemoveOntologyAnnotationChange) change;
        assertThat(removeOntologyAnnotationChange.getOntologyId(), is(ontologyId));
        assertThat(removeOntologyAnnotationChange.getAnnotation(), is(ontologyAnnotation));
    }

    @Test
    public void shouldVisitAddImportsChange() {
        var addImports = new AddImport(ontology, importsDecl);
        var change = addImports.accept(visitor);
        assertThat(change, is(instanceOf(AddImportChange.class)));
        var addImportsChange = (AddImportChange) change;
        assertThat(addImportsChange.getOntologyId(), is(ontologyId));
        assertThat(addImportsChange.getImportsDeclaration(), is(importsDecl));
    }

    @Test
    public void shouldVisitRemoveImportsChange() {
        var removeImports = new RemoveImport(ontology, importsDecl);
        var change = removeImports.accept(visitor);
        assertThat(change, is(instanceOf(RemoveImportChange.class)));
        var removeImportsChange = (RemoveImportChange) change;
        assertThat(removeImportsChange.getOntologyId(), is(ontologyId));
        assertThat(removeImportsChange.getImportsDeclaration(), is(importsDecl));
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionForSetOntologyId() {
        assertThrows(UnsupportedOperationException.class, () -> {
            var setOntologyId = new SetOntologyID(ontology, mock(OWLOntologyID.class));
            setOntologyId.accept(visitor);
        });
    }
}
