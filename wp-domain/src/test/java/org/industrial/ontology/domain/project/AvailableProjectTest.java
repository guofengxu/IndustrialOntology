package org.industrial.ontology.domain.project;

import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.industrial.ontology.domain.core.ProjectId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.AvailableProject_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AvailableProjectTest {

    private static final String DISPLAY_NAME = "DisplayName";

    private static final String DESCRIPTION = "Description";

    public static final long CREATED_AT = 33L;

    public static final long MODIFIED_AT = 55L;

    private AvailableProject availableProject;

    @Mock
    private ProjectDetails projectDetails;

    private boolean downloadable = true;

    private boolean trashable = true;

    @Mock
    private ProjectId projectId;

    @Mock
    private UserId modifiedBy, owner, createdBy;

    private long lastOpenedTimestamp = 11L;

    @BeforeEach
    public void setUp() {
        availableProject = AvailableProject.get(projectDetails, downloadable, trashable, lastOpenedTimestamp);
        when(projectDetails.getDisplayName()).thenReturn(DISPLAY_NAME);
        when(projectDetails.getDescription()).thenReturn(DESCRIPTION);
        when(projectDetails.getCreatedAt()).thenReturn(CREATED_AT);
        when(projectDetails.getCreatedBy()).thenReturn(createdBy);
        when(projectDetails.getLastModifiedAt()).thenReturn(MODIFIED_AT);
        when(projectDetails.getOwner()).thenReturn(owner);
        when(projectDetails.isInTrash()).thenReturn(trashable);
        when(projectDetails.getProjectId()).thenReturn(projectId);
        when(projectDetails.getLastModifiedBy()).thenReturn(modifiedBy);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_projectDetails_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            AvailableProject.get(null, downloadable, trashable, lastOpenedTimestamp);
        });
    }

    @Test
    public void shouldReturnSupplied_projectDetails() {
        assertThat(availableProject.getProjectDetails(), is(this.projectDetails));
    }

    @Test
    public void shouldReturnSupplied_downloadable() {
        assertThat(availableProject.isDownloadable(), is(this.downloadable));
    }

    @Test
    public void shouldReturnSupplied_trashable() {
        assertThat(availableProject.isTrashable(), is(this.trashable));
    }

    @Test
    public void shouldReturnSupplied_lastOpened() {
        assertThat(availableProject.getLastOpenedAt(), is(this.lastOpenedTimestamp));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(availableProject, is(availableProject));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(availableProject.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(availableProject, is(AvailableProject.get(projectDetails, downloadable, trashable, lastOpenedTimestamp)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_projectDetails() {
        assertThat(availableProject, is(not(AvailableProject.get(mock(ProjectDetails.class), downloadable, trashable, lastOpenedTimestamp))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_downloadable() {
        assertThat(availableProject, is(not(AvailableProject.get(projectDetails, false, trashable, lastOpenedTimestamp))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_trashable() {
        assertThat(availableProject, is(not(AvailableProject.get(projectDetails, downloadable, false, lastOpenedTimestamp))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_lastOpened() {
        assertThat(availableProject, is(not(AvailableProject.get(projectDetails, downloadable, trashable, 33L))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(availableProject.hashCode(), is(AvailableProject.get(projectDetails, downloadable, trashable, lastOpenedTimestamp).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(availableProject.toString(), startsWith("AvailableProject"));
    }

    @Test
    public void should_getProjectId() {
        assertThat(availableProject.getProjectId(), is(projectId));
    }

    @Test
    public void should_getOwner() {
        assertThat(availableProject.getOwner(), is(owner));
    }

    @Test
    public void should_getDescription() {
        assertThat(availableProject.getDescription(), is(DESCRIPTION));
    }

    @Test
    public void shouldReturn_true_For_isInTrash() {
        assertThat(availableProject.isInTrash(), is(true));
    }

    @Test
    public void should_getCreatedAt() {
        assertThat(availableProject.getCreatedAt(), is(CREATED_AT));
    }

    @Test
    public void should_getCreatedBy() {
        assertThat(availableProject.getCreatedBy(), is(createdBy));
    }

    @Test
    public void should_getLastModifiedAt() {
        assertThat(availableProject.getLastModifiedAt(), is(MODIFIED_AT));
    }

    @Test
    public void should_getLastModifiedBy() {
        assertThat(availableProject.getLastModifiedBy(), is(modifiedBy));
    }

    @Test
    public void shouldReturn_true_For_isDownloadable() {
        assertThat(availableProject.isDownloadable(), is(true));
    }

    @Test
    public void shouldReturn_true_For_isTrashable() {
        assertThat(availableProject.isTrashable(), is(true));
    }

    @Test
    public void should_getDisplayName() {
        assertThat(availableProject.getDisplayName(), is(DISPLAY_NAME));
    }
}
