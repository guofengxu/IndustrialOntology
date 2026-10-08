package org.industrial.ontology.compat;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * One ontology change of a test dataset, independent of either kernel, so that the same revisions can be written
 * and compared through the legacy and the ported change classes.
 */
record NeutralChange(@Nonnull Kind kind, @Nonnull OWLOntologyID ontologyId, @Nonnull Object subject) {

    enum Kind {
        ADD_AXIOM, REMOVE_AXIOM, ADD_ONTOLOGY_ANNOTATION, REMOVE_ONTOLOGY_ANNOTATION, ADD_IMPORT, REMOVE_IMPORT
    }

    NeutralChange {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(ontologyId);
        Objects.requireNonNull(subject);
    }

    static NeutralChange addAxiom(OWLOntologyID ontologyId, OWLAxiom axiom) {
        return new NeutralChange(Kind.ADD_AXIOM, ontologyId, axiom);
    }

    static NeutralChange removeAxiom(OWLOntologyID ontologyId, OWLAxiom axiom) {
        return new NeutralChange(Kind.REMOVE_AXIOM, ontologyId, axiom);
    }

    static NeutralChange addAnnotation(OWLOntologyID ontologyId, OWLAnnotation annotation) {
        return new NeutralChange(Kind.ADD_ONTOLOGY_ANNOTATION, ontologyId, annotation);
    }

    static NeutralChange removeAnnotation(OWLOntologyID ontologyId, OWLAnnotation annotation) {
        return new NeutralChange(Kind.REMOVE_ONTOLOGY_ANNOTATION, ontologyId, annotation);
    }

    static NeutralChange addImport(OWLOntologyID ontologyId, OWLImportsDeclaration declaration) {
        return new NeutralChange(Kind.ADD_IMPORT, ontologyId, declaration);
    }

    static NeutralChange removeImport(OWLOntologyID ontologyId, OWLImportsDeclaration declaration) {
        return new NeutralChange(Kind.REMOVE_IMPORT, ontologyId, declaration);
    }

    edu.stanford.bmir.protege.web.server.change.OntologyChange toLegacy() {
        return switch(kind) {
            case ADD_AXIOM -> edu.stanford.bmir.protege.web.server.change.AddAxiomChange
                    .of(ontologyId, (OWLAxiom) subject);
            case REMOVE_AXIOM -> edu.stanford.bmir.protege.web.server.change.RemoveAxiomChange
                    .of(ontologyId, (OWLAxiom) subject);
            case ADD_ONTOLOGY_ANNOTATION -> edu.stanford.bmir.protege.web.server.change.AddOntologyAnnotationChange
                    .of(ontologyId, (OWLAnnotation) subject);
            case REMOVE_ONTOLOGY_ANNOTATION -> edu.stanford.bmir.protege.web.server.change
                    .RemoveOntologyAnnotationChange.of(ontologyId, (OWLAnnotation) subject);
            case ADD_IMPORT -> edu.stanford.bmir.protege.web.server.change.AddImportChange
                    .of(ontologyId, (OWLImportsDeclaration) subject);
            case REMOVE_IMPORT -> edu.stanford.bmir.protege.web.server.change.RemoveImportChange
                    .of(ontologyId, (OWLImportsDeclaration) subject);
        };
    }

    org.industrial.ontology.kernel.api.change.OntologyChange toKernel() {
        return switch(kind) {
            case ADD_AXIOM -> org.industrial.ontology.kernel.api.change.AddAxiomChange
                    .of(ontologyId, (OWLAxiom) subject);
            case REMOVE_AXIOM -> org.industrial.ontology.kernel.api.change.RemoveAxiomChange
                    .of(ontologyId, (OWLAxiom) subject);
            case ADD_ONTOLOGY_ANNOTATION -> org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange
                    .of(ontologyId, (OWLAnnotation) subject);
            case REMOVE_ONTOLOGY_ANNOTATION -> org.industrial.ontology.kernel.api.change
                    .RemoveOntologyAnnotationChange.of(ontologyId, (OWLAnnotation) subject);
            case ADD_IMPORT -> org.industrial.ontology.kernel.api.change.AddImportChange
                    .of(ontologyId, (OWLImportsDeclaration) subject);
            case REMOVE_IMPORT -> org.industrial.ontology.kernel.api.change.RemoveImportChange
                    .of(ontologyId, (OWLImportsDeclaration) subject);
        };
    }

    static NeutralChange fromLegacy(edu.stanford.bmir.protege.web.server.change.OntologyChange change) {
        var ontologyId = change.getOntologyId();
        if(change.isAddAxiom()) {
            return addAxiom(ontologyId, change.getAxiomOrThrow());
        }
        if(change.isRemoveAxiom()) {
            return removeAxiom(ontologyId, change.getAxiomOrThrow());
        }
        if(change.isAddOntologyAnnotation()) {
            return addAnnotation(ontologyId, change.getAnnotationOrThrow());
        }
        if(change.isRemoveOntologyAnnotation()) {
            return removeAnnotation(ontologyId, change.getAnnotationOrThrow());
        }
        if(change instanceof edu.stanford.bmir.protege.web.server.change.AddImportChange) {
            return addImport(ontologyId, change.getImportsDeclarationOrThrow());
        }
        if(change instanceof edu.stanford.bmir.protege.web.server.change.RemoveImportChange) {
            return removeImport(ontologyId, change.getImportsDeclarationOrThrow());
        }
        throw new IllegalArgumentException("Unknown legacy change " + change);
    }

    static NeutralChange fromKernel(org.industrial.ontology.kernel.api.change.OntologyChange change) {
        var ontologyId = change.getOntologyId();
        if(change.isAddAxiom()) {
            return addAxiom(ontologyId, change.getAxiomOrThrow());
        }
        if(change.isRemoveAxiom()) {
            return removeAxiom(ontologyId, change.getAxiomOrThrow());
        }
        if(change.isAddOntologyAnnotation()) {
            return addAnnotation(ontologyId, change.getAnnotationOrThrow());
        }
        if(change.isRemoveOntologyAnnotation()) {
            return removeAnnotation(ontologyId, change.getAnnotationOrThrow());
        }
        if(change instanceof org.industrial.ontology.kernel.api.change.AddImportChange add) {
            return addImport(ontologyId, add.getImportsDeclaration());
        }
        if(change instanceof org.industrial.ontology.kernel.api.change.RemoveImportChange remove) {
            return removeImport(ontologyId, remove.getImportsDeclaration());
        }
        throw new IllegalArgumentException("Unknown change " + change);
    }
}
