package org.industrial.ontology.kernel.api.change;

import org.industrial.ontology.kernel.api.util.IriReplacer;
import org.semanticweb.owlapi.change.AddAxiomData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.AddAxiomChange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-26
 */
public record AddAxiomChange(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAxiom axiom) implements AxiomChange {

    public AddAxiomChange {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(axiom, "Null axiom");
    }

    public static AddAxiomChange of(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLAxiom axiom) {
        return new AddAxiomChange(ontologyId, axiom);
    }

    @Override
    public boolean isAddAxiom() {
        return true;
    }

    @Nonnull
    @Override
    public AddAxiomChange replaceIris(@Nonnull IriReplacer iriReplacer) {
        OWLAxiom duplicatedAxiom = iriReplacer.replaceIris(getAxiom());
        return AddAxiomChange.of(getOntologyId(), duplicatedAxiom);
    }

    @Nonnull
    @Override
    public AddAxiomChange replaceOntologyId(@Nonnull OWLOntologyID ontologyId) {
        if (ontologyId.equals(getOntologyId())) {
            return this;
        } else {
            return AddAxiomChange.of(ontologyId, getAxiom());
        }
    }

    @Nonnull
    @Override
    public OWLOntologyChangeRecord toOwlOntologyChangeRecord() {
        return new OWLOntologyChangeRecord(getOntologyId(), new AddAxiomData(getAxiom()));
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
    public RemoveAxiomChange getInverseChange() {
        return RemoveAxiomChange.of(getOntologyId(), getAxiom());
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
