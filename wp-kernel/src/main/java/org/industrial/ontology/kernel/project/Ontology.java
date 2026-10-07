package org.industrial.ontology.kernel.project;

import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.Ontology}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 *
 * A lightweight ontology object that contains an id, annotations and axiom.
 */
public record Ontology(@Nonnull OWLOntologyID ontologyId, @Nonnull ImmutableSet<OWLImportsDeclaration> importsDeclarations, @Nonnull ImmutableSet<OWLAnnotation> annotations, @Nonnull ImmutableSet<OWLAxiom> axioms) {

    public Ontology {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(importsDeclarations, "Null importsDeclarations");
        Objects.requireNonNull(annotations, "Null annotations");
        Objects.requireNonNull(axioms, "Null axioms");
    }

    public static Ontology get(@Nonnull OWLOntologyID ontologyId, @Nonnull Set<OWLImportsDeclaration> importsDeclarations, @Nonnull Set<OWLAnnotation> ontologyAnnotations, @Nonnull Set<OWLAxiom> ontologyAxioms) {
        return new Ontology(ontologyId, ImmutableSet.copyOf(importsDeclarations), ImmutableSet.copyOf(ontologyAnnotations), ImmutableSet.copyOf(ontologyAxioms));
    }

    @Nonnull
    public OWLOntologyID getOntologyId() {
        return ontologyId;
    }

    @Nonnull
    public ImmutableSet<OWLImportsDeclaration> getImportsDeclarations() {
        return importsDeclarations;
    }

    @Nonnull
    public ImmutableSet<OWLAnnotation> getAnnotations() {
        return annotations;
    }

    @Nonnull
    public ImmutableSet<OWLAxiom> getAxioms() {
        return axioms;
    }
}
