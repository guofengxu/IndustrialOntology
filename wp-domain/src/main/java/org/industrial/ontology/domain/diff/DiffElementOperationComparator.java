package org.industrial.ontology.domain.diff;



import java.util.Comparator;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.diff.DiffElementOperationComparator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public class DiffElementOperationComparator implements Comparator<DiffElement<?, ?>> {

    @Override
    public int compare(DiffElement<?, ?> o1, DiffElement<?, ?> o2) {
        return o1.getDiffOperation().compareTo(o2.getDiffOperation());
    }
}
