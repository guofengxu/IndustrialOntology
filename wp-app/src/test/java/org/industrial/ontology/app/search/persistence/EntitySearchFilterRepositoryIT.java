package org.industrial.ontology.app.search.persistence;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.EntityIsDeprecatedCriteria;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntitySearchFilterRepositoryIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static MongoPersistenceTestContext context;

    private EntitySearchFilterRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        repository = context.bean(EntitySearchFilterRepository.class);
    }

    @Test
    void shouldSaveFiltersByIdAndListThemByProject() {
        var deprecated = filter("aaaaaaaa-0000-4000-8000-000000000001", PROJECT, "Deprecated");
        var other = filter("aaaaaaaa-0000-4000-8000-000000000002", OTHER_PROJECT, "Other");
        repository.saveSearchFilters(ImmutableList.of(deprecated, other));
        var renamed = filter("aaaaaaaa-0000-4000-8000-000000000001", PROJECT, "Obsolete");

        repository.saveSearchFilters(ImmutableList.of(renamed));
        repository.saveSearchFilters(ImmutableList.of());

        assertThat(repository.getSearchFilters(PROJECT)).containsExactly(renamed);
        assertThat(repository.getSearchFilters(OTHER_PROJECT)).containsExactly(other);
    }

    private static EntitySearchFilter filter(String id, ProjectId projectId, String label) {
        return EntitySearchFilter.get(EntitySearchFilterId.get(id), projectId, LanguageMap.of("en", label),
                                      EntityIsDeprecatedCriteria.get());
    }
}
