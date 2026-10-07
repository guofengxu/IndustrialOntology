package org.industrial.ontology.domain.sharing;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.sharing.ProjectSharingSettings_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectSharingSettingsTest {

    private ProjectSharingSettings projectSharingSettings;

    @Mock
    private ProjectId projectId;

    private Optional<SharingPermission> linkSharingPermission = Optional.of(SharingPermission.EDIT);

    private List<SharingSetting> sharingSettings = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        projectSharingSettings = new ProjectSharingSettings(projectId, linkSharingPermission, sharingSettings);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_projectId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectSharingSettings(null, linkSharingPermission, sharingSettings);
        });
    }

    @Test
    public void shouldReturnSupplied_projectId() {
        assertThat(projectSharingSettings.getProjectId(), is(this.projectId));
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_linkSharingPermission_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectSharingSettings(projectId, null, sharingSettings);
        });
    }

    @Test
    public void shouldReturnSupplied_linkSharingPermission() {
        assertThat(projectSharingSettings.getLinkSharingPermission(), is(this.linkSharingPermission));
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_sharingSettings_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectSharingSettings(projectId, linkSharingPermission, null);
        });
    }

    @Test
    public void shouldReturnSupplied_sharingSettings() {
        assertThat(projectSharingSettings.getSharingSettings(), is(this.sharingSettings));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(projectSharingSettings, is(projectSharingSettings));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(projectSharingSettings.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(projectSharingSettings, is(new ProjectSharingSettings(projectId, linkSharingPermission, sharingSettings)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_projectId() {
        assertThat(projectSharingSettings, is(not(new ProjectSharingSettings(mock(ProjectId.class), linkSharingPermission, sharingSettings))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_linkSharingPermission() {
        assertThat(projectSharingSettings, is(not(new ProjectSharingSettings(projectId, Optional.of(SharingPermission.COMMENT), sharingSettings))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_sharingSettings() {
        ArrayList<SharingSetting> otherSharingSettings = new ArrayList<>();
        otherSharingSettings.add(mock(SharingSetting.class));
        assertThat(projectSharingSettings, is(not(new ProjectSharingSettings(projectId, linkSharingPermission, otherSharingSettings))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(projectSharingSettings.hashCode(), is(new ProjectSharingSettings(projectId, linkSharingPermission, sharingSettings).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(projectSharingSettings.toString(), startsWith("ProjectSharingSettings"));
    }
}
