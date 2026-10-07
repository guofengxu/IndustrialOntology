package org.industrial.ontology.kernel.form.data;



import com.google.common.collect.Comparators;
import org.industrial.ontology.domain.form.data.ImageControlDataDto;
import org.semanticweb.owlapi.model.IRI;

import java.util.Comparator;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.ImageControlDataDtoComparator}.
 */
public class ImageControlDataDtoComparator implements Comparator<ImageControlDataDto> {

    private static final Comparator<Optional<IRI>> optionalComparator = Comparators.emptiesLast(
            IRI::compareTo
    );

    public ImageControlDataDtoComparator() {
    }

    @Override
    public int compare(ImageControlDataDto o1, ImageControlDataDto o2) {
        return optionalComparator.compare(o1.getIri(), o2.getIri());
    }
}
