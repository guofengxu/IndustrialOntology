package org.industrial.ontology.kernel.api.repository;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.search.EntitySearchFilter;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.repository.ProjectEntitySearchFiltersManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-15
 */
public interface ProjectEntitySearchFiltersManager {

    @Nonnull
    ImmutableList<EntitySearchFilter> getSearchFilters();

    void setSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> searchFilters);
}
