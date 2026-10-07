package org.industrial.ontology.domain.search;

import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntitySearchFilter_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntitySearchFilterTest {

    @Mock
    private EntitySearchFilterId id;

    @Mock
    private ProjectId projectId;

    @Mock
    private LanguageMap label;

    @Mock
    private EntityMatchCriteria matchCriteria;

    private EntitySearchFilter filter;

    @BeforeEach
    public void setUp() throws Exception {
        filter = EntitySearchFilter.get(id, projectId, label, matchCriteria);
    }

    @Test
    public void shouldReturnProvidedId() {
        assertThat(filter.getId(), is(id));
    }

    @Test
    public void shouldReturnProvidedProjectId() {
        assertThat(filter.getProjectId(), is(projectId));
    }

    @Test
    public void shouldReturnProvidedLabel() {
        assertThat(filter.getLabel(), is(label));
    }

    @Test
    public void shouldReturnProvidedCriteria() {
        assertThat(filter.getEntityMatchCriteria(), is(matchCriteria));
    }
}
