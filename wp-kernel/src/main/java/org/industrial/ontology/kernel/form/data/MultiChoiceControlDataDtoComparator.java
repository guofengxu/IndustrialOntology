package org.industrial.ontology.kernel.form.data;



import com.google.common.collect.Comparators;
import org.industrial.ontology.domain.form.data.MultiChoiceControlDataDto;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.MultiChoiceControlDataDtoComparator}.
 */
public class MultiChoiceControlDataDtoComparator implements Comparator<MultiChoiceControlDataDto> {

    private static Comparator<Iterable<PrimitiveFormControlDataDto>> lexComparator = Comparators.lexicographical(
            PrimitiveFormControlDataDto::compareTo
    );

    public MultiChoiceControlDataDtoComparator() {
    }

    @Override
    public int compare(MultiChoiceControlDataDto o1, MultiChoiceControlDataDto o2) {
        return lexComparator.compare(o1.getValues(), o2.getValues());
    }
}
