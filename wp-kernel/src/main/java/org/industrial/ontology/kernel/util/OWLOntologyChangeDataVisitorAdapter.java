package org.industrial.ontology.kernel.util;



import javax.annotation.Nonnull;

import org.semanticweb.owlapi.change.AddOntologyAnnotationData;
import org.semanticweb.owlapi.change.SetOntologyIDData;
import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.RemoveImportData;
import org.semanticweb.owlapi.change.RemoveOntologyAnnotationData;
import org.semanticweb.owlapi.change.RemoveAxiomData;
import org.semanticweb.owlapi.change.OWLOntologyChangeDataVisitor;
import org.semanticweb.owlapi.change.AddAxiomData;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.OWLOntologyChangeDataVisitorAdapter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public class OWLOntologyChangeDataVisitorAdapter implements OWLOntologyChangeDataVisitor<Object, RuntimeException> {

    @Nonnull
    @Override
    public final Object visit(AddAxiomData data)  {
        visitAddAxiomData(data);
        return null;
    }

    public void visitAddAxiomData(AddAxiomData data)  {
    }

    @Override
    public final Object visit(RemoveAxiomData data)  {
        visitRemoveAxiomData(data);
        return null;
    }

    public void visitRemoveAxiomData(RemoveAxiomData data)  {
    }

    @Override
    public final Object visit(AddOntologyAnnotationData data)  {
        visitAddOntologyAnnotationData(data);
        return null;
    }

    public void visitAddOntologyAnnotationData(AddOntologyAnnotationData data) {

    }

    @Override
    public final Object visit(RemoveOntologyAnnotationData data)  {
        visitRemoveOntologyAnnotationData(data);
        return null;
    }

    public void visitRemoveOntologyAnnotationData(RemoveOntologyAnnotationData data) {

    }

    @Override
    public final Object visit(SetOntologyIDData data)  {
        visitSetOntologyIDData(data);
        return null;
    }

    public void visitSetOntologyIDData(SetOntologyIDData data) {

    }

    @Override
    public final Object visit(AddImportData data)  {
        visitAddImportData(data);
        return null;
    }

    public void visitAddImportData(AddImportData data) {

    }

    @Override
    public final Object visit(RemoveImportData data)  {
        visitRemoveImportData(data);
        return null;
    }

    public void visitRemoveImportData(RemoveImportData data) {

    }
}
