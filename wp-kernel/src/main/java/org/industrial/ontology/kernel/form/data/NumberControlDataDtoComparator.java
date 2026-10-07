package org.industrial.ontology.kernel.form.data;



import org.industrial.ontology.domain.form.data.NumberControlDataDto;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.NumberControlDataDtoComparator}.
 */
public class NumberControlDataDtoComparator implements Comparator<NumberControlDataDto> {

    public NumberControlDataDtoComparator() {
    }

    @Override
    public int compare(NumberControlDataDto o1, NumberControlDataDto o2) {
        return o1.compareTo(o2);
    }
}
