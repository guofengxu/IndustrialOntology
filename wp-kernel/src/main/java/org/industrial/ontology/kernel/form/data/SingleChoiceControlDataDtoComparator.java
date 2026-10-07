package org.industrial.ontology.kernel.form.data;



import com.google.common.collect.Comparators;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;
import org.industrial.ontology.domain.form.data.SingleChoiceControlDataDto;

import java.util.Comparator;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.SingleChoiceControlDataDtoComparator}.
 */
public class SingleChoiceControlDataDtoComparator implements Comparator<SingleChoiceControlDataDto> {

    private static Comparator<Optional<PrimitiveFormControlDataDto>> optionalComparator = Comparators.emptiesLast(
            PrimitiveFormControlDataDto::compareTo
    );

    public SingleChoiceControlDataDtoComparator() {
    }

    @Override
    public int compare(SingleChoiceControlDataDto o1, SingleChoiceControlDataDto o2) {
        return optionalComparator.compare(o1.getChoice(), o2.getChoice());
    }
}
