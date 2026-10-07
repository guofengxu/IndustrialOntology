package org.industrial.ontology.kernel.api.index;




import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.IndividualsByTypeIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
public interface IndividualsByTypeIndex extends Index {

    /**
     * Retrieve individuals that have the specified direct or indirect asserted type
     * @param type The type
     * @param retrievalMode specifying direct or indirect type retrieval
     */
    @Nonnull
    Stream<OWLNamedIndividual> getIndividualsByType(@Nonnull OWLClass type,
                                                    @Nonnull InstanceRetrievalMode retrievalMode);
}
