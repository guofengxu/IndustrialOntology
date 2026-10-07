package org.industrial.ontology.kernel.form.data;



import org.industrial.ontology.domain.form.data.TextControlDataDto;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.TextControlDataDtoComparator}.
 */
public class TextControlDataDtoComparator implements Comparator<TextControlDataDto> {

    public TextControlDataDtoComparator() {
    }

    @Override
    public int compare(TextControlDataDto o1, TextControlDataDto o2) {
        return o1.compareTo(o2);
    }
}
