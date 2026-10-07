package org.industrial.ontology.kernel.api.change;

import org.industrial.ontology.kernel.api.util.IriReplacer;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.change.RemoveOntologyAnnotationData;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.RemoveOntologyAnnotationChange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-26
 */
public record RemoveOntologyAnnotationChange(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAnnotation annotation) implements OntologyAnnotationChange {

    public RemoveOntologyAnnotationChange {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(annotation, "Null annotation");
    }

    public static RemoveOntologyAnnotationChange of(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAnnotation annotation) {
        return new RemoveOntologyAnnotationChange(ontologyId, annotation);
    }

    @Nonnull
    @Override
    public RemoveOntologyAnnotationChange replaceIris(@Nonnull IriReplacer iriReplacer) {
        OWLAnnotation duplicatedAnnotation = iriReplacer.replaceIris(getAnnotation());
        return RemoveOntologyAnnotationChange.of(getOntologyId(), duplicatedAnnotation);
    }

    @Nonnull
    @Override
    public RemoveOntologyAnnotationChange replaceOntologyId(@Nonnull OWLOntologyID ontologyId) {
        if (getOntologyId().equals(ontologyId)) {
            return this;
        } else {
            return RemoveOntologyAnnotationChange.of(ontologyId, getAnnotation());
        }
    }

    @Nonnull
    @Override
    public OWLOntologyChangeRecord toOwlOntologyChangeRecord() {
        return new OWLOntologyChangeRecord(getOntologyId(), new RemoveOntologyAnnotationData(getAnnotation()));
    }

    @Override
    public boolean isRemoveOntologyAnnotation() {
        return true;
    }

    @Override
    public void accept(@Nonnull OntologyChangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull OntologyChangeVisitorEx<R> visitorEx) {
        return visitorEx.visit(this);
    }

    @Nonnull
    @Override
    public AddOntologyAnnotationChange getInverseChange() {
        return AddOntologyAnnotationChange.of(getOntologyId(), getAnnotation());
    }

    @Override
    @Nonnull
    public OWLOntologyID getOntologyId() {
        return ontologyId;
    }

    @Override
    @Nonnull
    public OWLAnnotation getAnnotation() {
        return annotation;
    }
}
