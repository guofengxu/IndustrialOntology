package org.industrial.ontology.kernel.change;



import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;

import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitorEx;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.chg.OntologyChangeTranslatorVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-28
 */
public class OntologyChangeTranslatorVisitor implements OntologyChangeVisitorEx<OWLOntologyChange> {

    @Nonnull
    private final OWLOntologyManager manager;

    public OntologyChangeTranslatorVisitor(@Nonnull OWLOntologyManager manager) {
        this.manager = checkNotNull(manager);
    }

    @Override
    public OWLOntologyChange getDefaultReturnValue() {
        throw new RuntimeException();
    }

    @Override
    public OWLOntologyChange visit(@Nonnull AddAxiomChange change) {
        return new AddAxiom(getOntology(change), change.getAxiom());
    }

    @Override
    public OWLOntologyChange visit(@Nonnull RemoveAxiomChange change) {
        return new RemoveAxiom(getOntology(change), change.getAxiom());
    }

    @Override
    public OWLOntologyChange visit(@Nonnull AddOntologyAnnotationChange change) {
        return new AddOntologyAnnotation(getOntology(change), change.getAnnotation());
    }

    @Override
    public OWLOntologyChange visit(@Nonnull RemoveOntologyAnnotationChange change) {
        return new RemoveOntologyAnnotation(getOntology(change), change.getAnnotation());
    }

    @Override
    public OWLOntologyChange visit(@Nonnull AddImportChange change) {
        return new AddImport(getOntology(change), change.getImportsDeclaration());
    }

    @Override
    public OWLOntologyChange visit(@Nonnull RemoveImportChange change) {
        return new RemoveImport(getOntology(change), change.getImportsDeclaration());
    }



    private OWLOntology getOntology(OntologyChange change) {
        var ontologyId = change.getOntologyId();
        var ontology = manager.getOntology(ontologyId);
        if(ontology == null) {
            throw new UnknownOWLOntologyException(ontologyId);
        }
        else {
            return ontology;
        }
    }
}
