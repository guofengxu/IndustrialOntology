package org.industrial.ontology.app.admin;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.app.AccountCreationSetting;
import org.industrial.ontology.domain.app.ApplicationLocation;
import org.industrial.ontology.domain.app.ApplicationSettings;
import org.industrial.ontology.domain.app.NotificationEmailsSetting;
import org.industrial.ontology.domain.app.ProjectCreationSetting;
import org.industrial.ontology.domain.app.ProjectUploadSetting;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.EmailAddress;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The application settings: preferences in {@code ApplicationPreferences}, the account, project creation and upload
 * settings as application role assignments, and {@code EDIT_APPLICATION_SETTINGS} for reading and writing.
 */
class ApplicationSettingsServiceIT {

    private static final UserId ADMIN = UserId.getUserId("admin");

    private static final UserId EDITOR = UserId.getUserId("editor");

    private static MongoPersistenceTestContext context;

    private ApplicationSettingsService service;

    private AccessManager accessManager;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithServices();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        accessManager = context.bean(AccessManager.class);
        // A new service and repository each time: the repository caches the preferences.
        service = new ApplicationSettingsService(accessManager,
                                                 new ApplicationPreferencesRepository(context.mongoTemplate()));
        accessManager.setAssignedRoles(Subject.forUser(ADMIN), ApplicationResource.get(),
                                       List.of(BuiltInRole.SYSTEM_ADMIN.getRoleId()));
    }

    @Test
    void shouldReadTheLegacyDefaultsOfAnEmptyDatabase() {
        var settings = service.getApplicationSettings(ADMIN);

        assertThat(settings).isEqualTo(new ApplicationSettings("WebProtégé",
                                                               new EmailAddress(""),
                                                               new ApplicationLocation("https", "", "", 443),
                                                               AccountCreationSetting.ACCOUNT_CREATION_NOT_ALLOWED,
                                                               ImmutableList.of(),
                                                               ProjectCreationSetting
                                                                       .EMPTY_PROJECT_CREATION_NOT_ALLOWED,
                                                               ImmutableList.of(),
                                                               ProjectUploadSetting.PROJECT_UPLOAD_NOT_ALLOWED,
                                                               ImmutableList.of(),
                                                               NotificationEmailsSetting.SEND_NOTIFICATION_EMAILS,
                                                               Long.MAX_VALUE));
    }

    @Test
    void shouldWriteThePreferencesAndTheRoleAssignments() {
        service.setApplicationSettings(ADMIN, settings(AccountCreationSetting.ACCOUNT_CREATION_ALLOWED,
                                                       ProjectCreationSetting.EMPTY_PROJECT_CREATION_ALLOWED,
                                                       ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED));

        var reread = new ApplicationSettingsService(accessManager,
                                                    new ApplicationPreferencesRepository(context.mongoTemplate()))
                .getApplicationSettings(ADMIN);
        assertThat(reread).isEqualTo(settings(AccountCreationSetting.ACCOUNT_CREATION_ALLOWED,
                                              ProjectCreationSetting.EMPTY_PROJECT_CREATION_ALLOWED,
                                              ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED));
        assertThat(accessManager.hasPermission(Subject.forUser(EDITOR), ApplicationResource.get(),
                                               BuiltInAction.CREATE_EMPTY_PROJECT)).isTrue();
        assertThat(accessManager.getAssignedRoles(Subject.forGuestUser(), ApplicationResource.get()))
                .containsExactly(BuiltInRole.ACCOUNT_CREATOR.getRoleId());
    }

    @Test
    void shouldKeepOtherRolesWhenTurningASettingOff() {
        accessManager.setAssignedRoles(Subject.forAnySignedInUser(), ApplicationResource.get(),
                                       List.of(BuiltInRole.PROJECT_CREATOR.getRoleId(),
                                               BuiltInRole.PROJECT_UPLOADER.getRoleId()));

        service.setApplicationSettings(ADMIN, settings(AccountCreationSetting.ACCOUNT_CREATION_NOT_ALLOWED,
                                                       ProjectCreationSetting.EMPTY_PROJECT_CREATION_NOT_ALLOWED,
                                                       ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED));

        assertThat(accessManager.getAssignedRoles(Subject.forAnySignedInUser(), ApplicationResource.get()))
                .containsExactly(BuiltInRole.PROJECT_UPLOADER.getRoleId());
        assertThat(service.getApplicationSettings(ADMIN).getProjectCreationSetting())
                .isEqualTo(ProjectCreationSetting.EMPTY_PROJECT_CREATION_NOT_ALLOWED);
    }

    @Test
    void shouldRequireEditApplicationSettings() {
        var settings = settings(AccountCreationSetting.ACCOUNT_CREATION_NOT_ALLOWED,
                                ProjectCreationSetting.EMPTY_PROJECT_CREATION_ALLOWED,
                                ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED);

        assertThatThrownBy(() -> service.getApplicationSettings(EDITOR)).isInstanceOf(PermissionDeniedException.class)
                                                                       .hasMessageContaining("EditApplicationSettings");
        assertThatThrownBy(() -> service.setApplicationSettings(EDITOR, settings))
                .isInstanceOf(PermissionDeniedException.class);
        assertThat(accessManager.getAssignedRoles(Subject.forAnySignedInUser(), ApplicationResource.get())).isEmpty();
    }

    private static ApplicationSettings settings(AccountCreationSetting accountCreation,
                                                ProjectCreationSetting projectCreation,
                                                ProjectUploadSetting projectUpload) {
        return new ApplicationSettings("Ontology Hub",
                                       new EmailAddress("ops@example.org"),
                                       new ApplicationLocation("https", "ontology.example.org", "/", 443),
                                       accountCreation,
                                       ImmutableList.of(),
                                       projectCreation,
                                       ImmutableList.of(),
                                       projectUpload,
                                       ImmutableList.of(),
                                       NotificationEmailsSetting.SEND_NOTIFICATION_EMAILS,
                                       200L * 1024 * 1024);
    }
}
