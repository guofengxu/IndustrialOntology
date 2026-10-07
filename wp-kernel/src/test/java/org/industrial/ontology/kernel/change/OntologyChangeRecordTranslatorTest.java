package org.industrial.ontology.kernel.change;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.semanticweb.owlapi.change.RemoveAxiomData;
import org.semanticweb.owlapi.change.SetOntologyIDData;
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.change.RemoveOntologyAnnotationData;
import org.semanticweb.owlapi.change.AddAxiomData;
import org.semanticweb.owlapi.change.AddOntologyAnnotationData;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OntologyChangeRecordTranslatorImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-16
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyChangeRecordTranslatorTest {

    private OntologyChangeRecordTranslatorImpl impl;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLAnnotation annotation;

    @Mock
    private OWLImportsDeclaration importsDeclaration;

    @Mock
    private OWLOntologyID otherOntologyId;

    @BeforeEach
    public void setUp() {
        impl = new OntologyChangeRecordTranslatorImpl();
    }

    @Test
    public void shouldTranslateAddAxiom() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new AddAxiomData(axiom)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change.getAxiomOrThrow(), is(axiom));
        assertThat(change, is(instanceOf(AddAxiomChange.class)));
    }

    @Test
    public void shouldTranslateRemoveAxiom() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new RemoveAxiomData(axiom)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change.getAxiomOrThrow(), is(axiom));
        assertThat(change, is(instanceOf(RemoveAxiomChange.class)));
    }

    @Test
    public void shouldTranslateAddOntologyAnnotation() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new AddOntologyAnnotationData(annotation)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change, is(instanceOf(AddOntologyAnnotationChange.class)));
        assertThat(((AddOntologyAnnotationChange) change).getAnnotation(), is(annotation));
    }

    @Test
    public void shouldTranslateRemoveOntologyAnnotation() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new RemoveOntologyAnnotationData(annotation)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change, is(instanceOf(RemoveOntologyAnnotationChange.class)));
        assertThat(((RemoveOntologyAnnotationChange) change).getAnnotation(), is(annotation));
    }

    @Test
    public void shouldTranslateAddImport() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new AddImportData(importsDeclaration)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change, is(instanceOf(AddImportChange.class)));
        assertThat(((AddImportChange) change).getImportsDeclaration(), is(importsDeclaration));
    }

    @Test
    public void shouldTranslateRemoveImport() {
        var change = impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new RemoveImportData(importsDeclaration)));
        assertThat(change.getOntologyId(), is(ontologyId));
        assertThat(change, is(instanceOf(RemoveImportChange.class)));
        assertThat(((RemoveImportChange) change).getImportsDeclaration(), is(importsDeclaration));
    }

    @Test
    public void shouldNotTranslateSetOntologyID() {
        assertThrows(UnsupportedOperationException.class, () -> {
            impl.getOntologyChange(new OWLOntologyChangeRecord(ontologyId, new SetOntologyIDData(otherOntologyId)));
        });
    }
}
