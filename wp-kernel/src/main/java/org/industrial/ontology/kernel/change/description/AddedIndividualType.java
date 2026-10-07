package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedIndividualType}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedIndividualType(@Nonnull OWLIndividual individual, @Nonnull OWLClass type) implements StructuredChangeDescription {

    public AddedIndividualType {
        Objects.requireNonNull(individual, "Null individual");
        Objects.requireNonNull(type, "Null type");
    }

    @Nonnull
    public static AddedIndividualType get(@Nonnull OWLIndividual individual, @Nonnull OWLClass type) {
        return new AddedIndividualType(individual, type);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedIndividualType";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added %s as a type to %s", getType(), getIndividual());
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
