package org.industrial.ontology.kernel.change;



import javax.annotation.Nonnull;

import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OwlOntologyChangeTranslatorVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public class OwlOntologyChangeTranslatorVisitor implements OWLOntologyChangeVisitorEx<OntologyChange> {

    public OwlOntologyChangeTranslatorVisitor() {
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull AddAxiom change) {
        return AddAxiomChange.of(change.getOntology().getOntologyID(),
                                 change.getAxiom());
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull RemoveAxiom change) {
        return RemoveAxiomChange.of(change.getOntology().getOntologyID(),
                                    change.getAxiom());
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull SetOntologyID change) {
        throw new UnsupportedOperationException("SetOntologyID changes are not supported");
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull AddImport change) {
        return AddImportChange.of(change.getOntology().getOntologyID(),
                                  change.getImportDeclaration());
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull RemoveImport change) {
        return RemoveImportChange.of(change.getOntology().getOntologyID(),
                                     change.getImportDeclaration());
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull AddOntologyAnnotation change) {
        return AddOntologyAnnotationChange.of(change.getOntology().getOntologyID(),
                                              change.getAnnotation());
    }

    @Nonnull
    @Override
    public OntologyChange visit(@Nonnull RemoveOntologyAnnotation change) {
        return RemoveOntologyAnnotationChange.of(change.getOntology().getOntologyID(),
                                                 change.getAnnotation());
    }
}
