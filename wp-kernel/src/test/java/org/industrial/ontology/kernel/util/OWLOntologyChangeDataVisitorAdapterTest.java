package org.industrial.ontology.kernel.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.change.RemoveAxiomData;
import org.semanticweb.owlapi.change.SetOntologyIDData;
import org.semanticweb.owlapi.change.AddOntologyAnnotationData;
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.change.RemoveOntologyAnnotationData;
import org.semanticweb.owlapi.change.AddAxiomData;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.OWLOntologyChangeDataVisitorAdapter_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OWLOntologyChangeDataVisitorAdapterTest {

    @Spy
    private OWLOntologyChangeDataVisitorAdapter adapter = new OWLOntologyChangeDataVisitorAdapter();

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void shouldVisitAddAxiomData() {
        var addAxiomData = new AddAxiomData(mock(OWLAxiom.class));
        addAxiomData.accept(adapter);
        verify(adapter, times(1)).visitAddAxiomData(addAxiomData);
    }

    @Test
    public void shouldVisitRemoveAxiomData() {
        var removeAxiomData = new RemoveAxiomData(mock(OWLAxiom.class));
        removeAxiomData.accept(adapter);
        verify(adapter, times(1)).visitRemoveAxiomData(removeAxiomData);
    }

    @Test
    public void shouldVisitAddImportData() {
        var addImportData = new AddImportData(mock(OWLImportsDeclaration.class));
        addImportData.accept(adapter);
        verify(adapter, times(1)).visitAddImportData(addImportData);
    }

    @Test
    public void shouldVisitRemoveImportData() {
        var removeImportData = new RemoveImportData(mock(OWLImportsDeclaration.class));
        removeImportData.accept(adapter);
        verify(adapter, times(1)).visitRemoveImportData(removeImportData);
    }

    @Test
    public void shouldVisitSetOntologyId() {
        var setOntologyId = new SetOntologyIDData(mock(OWLOntologyID.class));
        setOntologyId.accept(adapter);
        verify(adapter, times(1)).visitSetOntologyIDData(setOntologyId);
    }

    @Test
    public void shouldVisitAddOntologyAnnotationData() {
        var addOntologyAnnotationData = new AddOntologyAnnotationData(mock(OWLAnnotation.class));
        addOntologyAnnotationData.accept(adapter);
        verify(adapter, times(1)).visitAddOntologyAnnotationData(addOntologyAnnotationData);
    }

    @Test
    public void shouldVisitRemoveOntologyAnnotationData() {
        var removeOntologyAnnotationData = new RemoveOntologyAnnotationData(mock(OWLAnnotation.class));
        removeOntologyAnnotationData.accept(adapter);
        verify(adapter, times(1)).visitRemoveOntologyAnnotationData(removeOntologyAnnotationData);
    }
}
