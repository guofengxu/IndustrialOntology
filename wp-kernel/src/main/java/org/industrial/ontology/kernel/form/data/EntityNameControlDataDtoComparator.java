package org.industrial.ontology.kernel.form.data;



import com.google.common.collect.Comparators;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.EntityNameControlDataDto;

import java.util.Comparator;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.data.EntityNameControlDataDtoComparator}.
 */
public class EntityNameControlDataDtoComparator implements Comparator<EntityNameControlDataDto> {

    private static final Comparator<Optional<OWLEntityData>> optionalEntityDataComparator = Comparators.emptiesLast(
            OWLEntityData::compareToIgnoreCase
    );

    public EntityNameControlDataDtoComparator() {
    }

    @Override
    public int compare(EntityNameControlDataDto o1, EntityNameControlDataDto o2) {
        return optionalEntityDataComparator.compare(o1.getEntity(), o2.getEntity());
    }
}
