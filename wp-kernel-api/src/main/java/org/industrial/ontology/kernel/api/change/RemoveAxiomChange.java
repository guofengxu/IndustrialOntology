package org.industrial.ontology.kernel.api.change;

import org.industrial.ontology.kernel.api.util.IriReplacer;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.change.RemoveAxiomData;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.RemoveAxiomChange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-26
 */
public record RemoveAxiomChange(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAxiom axiom) implements AxiomChange {

    public RemoveAxiomChange {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(axiom, "Null axiom");
    }

    public static RemoveAxiomChange of(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAxiom axiom) {
        return new RemoveAxiomChange(ontologyId, axiom);
    }

    @Override
    public boolean isRemoveAxiom() {
        return true;
    }

    @Nonnull
    @Override
    public RemoveAxiomChange replaceIris(@Nonnull IriReplacer iriReplacer) {
        OWLAxiom duplicatedAxiom = iriReplacer.replaceIris(getAxiom());
        return RemoveAxiomChange.of(getOntologyId(), duplicatedAxiom);
    }

    @Nonnull
    @Override
    public RemoveAxiomChange replaceOntologyId(@Nonnull OWLOntologyID ontologyId) {
        if (getOntologyId().equals(ontologyId)) {
            return this;
        } else {
            return RemoveAxiomChange.of(ontologyId, getAxiom());
        }
    }

    @Nonnull
    @Override
    public AddAxiomChange getInverseChange() {
        return AddAxiomChange.of(getOntologyId(), getAxiom());
    }

    @Nonnull
    @Override
    public OWLOntologyChangeRecord toOwlOntologyChangeRecord() {
        return new OWLOntologyChangeRecord(getOntologyId(), new RemoveAxiomData(getAxiom()));
    }

    @Override
    public void accept(@Nonnull OntologyChangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull OntologyChangeVisitorEx<R> visitorEx) {
        return visitorEx.visit(this);
    }

    @Override
    @Nonnull
    public OWLOntologyID getOntologyId() {
        return ontologyId;
    }

    @Override
    @Nonnull
    public OWLAxiom getAxiom() {
        return axiom;
    }
}
