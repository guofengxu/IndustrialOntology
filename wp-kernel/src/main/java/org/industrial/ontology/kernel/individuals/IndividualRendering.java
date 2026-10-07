package org.industrial.ontology.kernel.individuals;



import org.industrial.ontology.kernel.util.AlphaNumericStringComparator;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.individuals.IndividualRendering}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
public class IndividualRendering implements Comparable<IndividualRendering> {

    private final OWLNamedIndividual individual;

    private final String rendering;

    private final AlphaNumericStringComparator comparator = new AlphaNumericStringComparator();

    public IndividualRendering(OWLNamedIndividual individual, String rendering) {
        this.individual = individual;
        this.rendering = rendering;
    }

    public OWLNamedIndividual getIndividual() {
        return individual;
    }

    public String getRendering() {
        return rendering;
    }

    @Override
    public int compareTo(@Nonnull IndividualRendering o) {
        return comparator.compare(this.rendering, o.rendering);
    }
}
