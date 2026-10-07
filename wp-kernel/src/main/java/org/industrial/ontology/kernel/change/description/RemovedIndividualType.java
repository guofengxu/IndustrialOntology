package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.RemovedIndividualType}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record RemovedIndividualType(@Nonnull OWLIndividual individual, @Nonnull OWLClass type) implements StructuredChangeDescription {

    public RemovedIndividualType {
        Objects.requireNonNull(individual, "Null individual");
        Objects.requireNonNull(type, "Null type");
    }

    public static RemovedIndividualType get(@Nonnull OWLIndividual individual, @Nonnull OWLClass type) {
        return new RemovedIndividualType(individual, type);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "RemovedIndividualType";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Removed %s as a type from %s", getType(), getIndividual());
    }

    @Nonnull
    public OWLIndividual getIndividual() {
        return individual;
    }

    @Nonnull
    public OWLClass getType() {
        return type;
    }
}
