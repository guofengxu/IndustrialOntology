package org.industrial.ontology.kernel.axiom;



import com.google.common.collect.Ordering;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Arrays;
import java.util.Comparator;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.axiom.AxiomComparatorImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
public class AxiomComparator implements Comparator<OWLAxiom> {

    private Comparator<OWLAxiom> compoundComparator;

    public AxiomComparator(AxiomBySubjectComparator axiomBySubjectComparator, AxiomByTypeComparator axiomByTypeComparator, AxiomByRenderingComparator axiomByRenderingComparator) {
        compoundComparator = Ordering.compound(
                Arrays.asList(
                        checkNotNull(axiomBySubjectComparator),
                        checkNotNull(axiomByTypeComparator),
                        checkNotNull(axiomByRenderingComparator)));
    }

    @Override
    public int compare(OWLAxiom o1, OWLAxiom o2) {
        return compoundComparator.compare(o1, o2);
    }
}
