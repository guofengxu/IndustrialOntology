package org.industrial.ontology.domain.tag;

import java.util.Collection;
import java.util.Collections;
import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import org.industrial.ontology.domain.event.EntityTagsChangedEvent;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.EntityTagsChangedEvent_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityTagsChangedEventTest {

    private EntityTagsChangedEvent event;

    @Mock
    private ProjectId projectId;

    @Mock
    private OWLEntity entity;

    @Mock
    private Collection<Tag> tags;

    @BeforeEach
    public void setUp() {
        tags = Collections.singleton(mock(Tag.class));
        event = new EntityTagsChangedEvent(projectId, entity, tags);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_projectId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new EntityTagsChangedEvent(null, entity, tags);
        });
    }

    @Test
    public void shouldReturnSupplied_projectId() {
        assertThat(event.getProjectId(), is(this.projectId));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_entity_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new EntityTagsChangedEvent(projectId, null, tags);
        });
    }

    @Test
    public void shouldReturnSupplied_entity() {
        assertThat(event.getEntity(), is(this.entity));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_tags_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new EntityTagsChangedEvent(projectId, entity, null);
        });
    }

    @Test
    public void shouldReturnSupplied_tags() {
        assertThat(event.getTags(), is(this.tags));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(event, is(event));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(event.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(event, is(new EntityTagsChangedEvent(projectId, entity, tags)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_projectId() {
        assertThat(event, is(not(new EntityTagsChangedEvent(mock(ProjectId.class), entity, tags))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_entity() {
        assertThat(event, is(not(new EntityTagsChangedEvent(projectId, mock(OWLEntity.class), tags))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_tags() {
        assertThat(event, is(not(new EntityTagsChangedEvent(projectId, entity, Collections.singleton(mock(Tag.class))))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(event.hashCode(), is(new EntityTagsChangedEvent(projectId, entity, tags).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(event.toString(), startsWith("EntityTagsChangedEvent"));
    }
}
