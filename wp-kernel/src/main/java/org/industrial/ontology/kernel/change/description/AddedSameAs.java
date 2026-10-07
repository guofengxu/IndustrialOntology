package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLIndividual;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedSameAs}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedSameAs(ImmutableSet<OWLIndividual> individuals) implements StructuredChangeDescription {

    public AddedSameAs {
        Objects.requireNonNull(individuals, "Null individuals");
    }

    public static AddedSameAs get(@Nonnull ImmutableSet<OWLIndividual> individuals) {
        return new AddedSameAs(individuals);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedSameAs";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added SameAs between %s", getIndividuals());
    }

    public ImmutableSet<OWLIndividual> getIndividuals() {
        return individuals;
    }
}
