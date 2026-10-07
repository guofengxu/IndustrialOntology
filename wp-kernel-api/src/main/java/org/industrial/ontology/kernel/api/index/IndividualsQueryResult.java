package org.industrial.ontology.kernel.api.index;

import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.industrial.ontology.domain.pagination.Page;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.IndividualsQueryResult}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Sep 2018
 */
public record IndividualsQueryResult(@Nonnull Page<OWLNamedIndividual> individuals, long individualsCount, @Nonnull OWLClass type, @Nonnull InstanceRetrievalMode mode) {

    public IndividualsQueryResult {
        Objects.requireNonNull(individuals, "Null individuals");
        Objects.requireNonNull(type, "Null type");
        Objects.requireNonNull(mode, "Null mode");
    }

    public static IndividualsQueryResult get(Page<OWLNamedIndividual> queryResultPage, long individualsCount, OWLClass type, InstanceRetrievalMode mode) {
        return new IndividualsQueryResult(queryResultPage, individualsCount, type, mode);
    }

    @Nonnull
    public Page<OWLNamedIndividual> getIndividuals() {
        return individuals;
    }

    public long getIndividualsCount() {
        return individualsCount;
    }

    @Nonnull
    public OWLClass getType() {
        return type;
    }

    @Nonnull
    public InstanceRetrievalMode getMode() {
        return mode;
    }
}
