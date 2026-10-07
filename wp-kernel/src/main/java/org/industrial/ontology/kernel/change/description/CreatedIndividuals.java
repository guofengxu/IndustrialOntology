package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.CreatedIndividuals}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record CreatedIndividuals(@Nonnull ImmutableSet<OWLNamedIndividual> individuals, @Nonnull ImmutableSet<OWLClass> types) implements StructuredChangeDescription {

    public CreatedIndividuals {
        Objects.requireNonNull(individuals, "Null individuals");
        Objects.requireNonNull(types, "Null types");
    }

    public static CreatedIndividuals get(@Nonnull ImmutableSet<OWLNamedIndividual> individuals, @Nonnull ImmutableSet<OWLClass> types) {
        return new CreatedIndividuals(individuals, types);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "CreatedIndividuals";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        if (getTypes().isEmpty()) {
            if (getIndividuals().size() == 1) {
                return formatter.formatString("Created individual %s", getIndividuals());
            } else {
                return formatter.formatString("Created individuals %s", getIndividuals());
            }
        } else {
            if (getIndividuals().size() == 1) {
                return formatter.formatString("Created %s as an instance of %s", getIndividuals(), getTypes());
            } else {
                return formatter.formatString("Created %s as instances of %s", getIndividuals(), getTypes());
            }
        }
    }

    @Nonnull
    public ImmutableSet<OWLNamedIndividual> getIndividuals() {
        return individuals;
    }

    @Nonnull
    public ImmutableSet<OWLClass> getTypes() {
        return types;
    }
}
