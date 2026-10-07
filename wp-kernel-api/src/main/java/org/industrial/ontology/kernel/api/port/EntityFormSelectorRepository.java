package org.industrial.ontology.kernel.api.port;



import org.industrial.ontology.domain.form.EntityFormSelector;
import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityFormSelectorRepository}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-08
 */
public interface EntityFormSelectorRepository {

    void save(EntityFormSelector entityFormSelector);

    Stream<EntityFormSelector> findFormSelectors(@Nonnull ProjectId projectId);
}
