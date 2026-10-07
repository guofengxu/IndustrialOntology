package org.industrial.ontology.kernel.mansyntax;



import com.google.common.collect.Lists;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.semanticweb.owlapi.util.OntologyAxiomPair;

import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.OntologyAxiomPairChangeGenerator}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/03/2014
 */
@SuppressWarnings("ConstantConditions")
public class OntologyAxiomPairChangeGenerator {

    public OntologyAxiomPairChangeGenerator() {
    }

    public List<OntologyChange> generateChanges(Set<OntologyAxiomPair> fromPairs, Set<OntologyAxiomPair> toPairs) {
        List<OntologyChange> result = Lists.newArrayList();
        for(OntologyAxiomPair fromPair : fromPairs) {
            if(!toPairs.contains(fromPair)) {
                result.add(RemoveAxiomChange.of(checkNotNull(fromPair.getOntology().getOntologyID()), fromPair.getAxiom()));
            }
        }
        for(OntologyAxiomPair toPair : toPairs) {
            if(!fromPairs.contains(toPair)) {
                result.add(AddAxiomChange.of(checkNotNull(toPair.getOntology().getOntologyID()), toPair.getAxiom()));
            }
        }
        return result;
    }
}
