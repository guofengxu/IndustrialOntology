package org.industrial.ontology.kernel.api.index;



import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DeprecatedEntitiesIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-04
 */
public interface DeprecatedEntitiesIndex {

    @Nonnull
    Page<OWLEntity> getDeprecatedEntities(@Nonnull Set<EntityType<?>> entityTypes,
                                          @Nonnull PageRequest pageRequest);
}
