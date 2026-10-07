package org.industrial.ontology.kernel.api.change;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OntologyAxiomPair}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2 Oct 2018
 */
public record OntologyAxiomPair(@Nonnull OWLOntologyID ontology, @Nonnull OWLAxiom axiom) {

    public OntologyAxiomPair {
        Objects.requireNonNull(ontology, "Null ontology");
        Objects.requireNonNull(axiom, "Null axiom");
    }

    public static OntologyAxiomPair get(@Nonnull OWLOntologyID ontology, @Nonnull OWLAxiom axiom) {
        return new OntologyAxiomPair(ontology, axiom);
    }

    @Nonnull
    public OWLOntologyID getOntology() {
        return ontology;
    }

    @Nonnull
    public OWLAxiom getAxiom() {
        return axiom;
    }
}
