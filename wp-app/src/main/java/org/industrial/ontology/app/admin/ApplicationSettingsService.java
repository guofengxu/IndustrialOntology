package org.industrial.ontology.app.admin;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesDocument;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.domain.app.AccountCreationSetting;
import org.industrial.ontology.domain.app.ApplicationSettings;
import org.industrial.ontology.domain.app.NotificationEmailsSetting;
import org.industrial.ontology.domain.app.ProjectCreationSetting;
import org.industrial.ontology.domain.app.ProjectUploadSetting;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.EmailAddress;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.app.ApplicationSettingsManager}, with the permission checks
 * of the legacy {@code Get/SetApplicationSettingsActionHandler}: both require {@code EDIT_APPLICATION_SETTINGS}.
 * <p>
 * The settings are stored in two places, as before: the name, notification address, location and upload limit in
 * {@code ApplicationPreferences}, and who may create accounts, create projects and upload projects as application
 * role assignments of the guest user ({@code AccountCreator}) and of any signed-in user ({@code ProjectCreator},
 * {@code ProjectUploader}).
 * <p>
 * Accounts are created in Keycloak now (docs/00 D5), so the account creation setting is kept for the data but grants
 * nothing in this application. The lists of account creators, project creators and uploaders were always empty in the
 * legacy settings and stay so; notification e-mails are always sent.
 */
public class ApplicationSettingsService {

    private final AccessManager accessManager;

    private final ApplicationPreferencesRepository preferencesRepository;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public ApplicationSettingsService(@Nonnull AccessManager accessManager,
                                      @Nonnull ApplicationPreferencesRepository preferencesRepository) {
        this.accessManager = checkNotNull(accessManager);
        this.preferencesRepository = checkNotNull(preferencesRepository);
    }

    @Nonnull
    public ApplicationSettings getApplicationSettings(@Nonnull UserId caller) {
        accessManager.require(caller, ApplicationResource.get(), BuiltInAction.EDIT_APPLICATION_SETTINGS);
        readLock.lock();
        try {
            var preferences = preferencesRepository.getApplicationPreferences();
            var application = ApplicationResource.get();
            var accountCreation = accessManager.hasPermission(Subject.forGuestUser(), application,
                                                              BuiltInAction.CREATE_ACCOUNT)
                    ? AccountCreationSetting.ACCOUNT_CREATION_ALLOWED
                    : AccountCreationSetting.ACCOUNT_CREATION_NOT_ALLOWED;
            var projectCreation = accessManager.hasPermission(Subject.forAnySignedInUser(), application,
                                                              BuiltInAction.CREATE_EMPTY_PROJECT)
                    ? ProjectCreationSetting.EMPTY_PROJECT_CREATION_ALLOWED
                    : ProjectCreationSetting.EMPTY_PROJECT_CREATION_NOT_ALLOWED;
            var projectUpload = accessManager.hasPermission(Subject.forAnySignedInUser(), application,
                                                            BuiltInAction.UPLOAD_PROJECT)
                    ? ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED
                    : ProjectUploadSetting.PROJECT_UPLOAD_NOT_ALLOWED;
            return new ApplicationSettings(preferences.applicationName(),
                                           new EmailAddress(preferences.systemNotificationEmailAddress()),
                                           preferences.applicationLocation().toApplicationLocation(),
                                           accountCreation,
                                           ImmutableList.of(),
                                           projectCreation,
                                           ImmutableList.of(),
                                           projectUpload,
                                           ImmutableList.of(),
                                           NotificationEmailsSetting.SEND_NOTIFICATION_EMAILS,
                                           preferences.maxUploadSize());
        } finally {
            readLock.unlock();
        }
    }

    public void setApplicationSettings(@Nonnull UserId caller, @Nonnull ApplicationSettings settings) {
        accessManager.require(caller, ApplicationResource.get(), BuiltInAction.EDIT_APPLICATION_SETTINGS);
        writeLock.lock();
        try {
            preferencesRepository.setApplicationPreferences(ApplicationPreferencesDocument.of(
                    settings.getApplicationName(),
                    settings.getSystemNotificationEmailAddress().getEmailAddress(),
                    settings.getApplicationLocation(),
                    settings.getMaxUploadSize()));

            var application = ApplicationResource.get();
            // Hash sets as in the legacy manager, so the stored role order is the same.
            Set<RoleId> guestRoles = new HashSet<>(accessManager.getAssignedRoles(Subject.forGuestUser(),
                                                                                  application));
            toggle(guestRoles, BuiltInRole.ACCOUNT_CREATOR,
                   settings.getAccountCreationSetting() == AccountCreationSetting.ACCOUNT_CREATION_ALLOWED);
            accessManager.setAssignedRoles(Subject.forGuestUser(), application, guestRoles);

            Set<RoleId> signedInRoles = new HashSet<>(accessManager.getAssignedRoles(Subject.forAnySignedInUser(),
                                                                                     application));
            toggle(signedInRoles, BuiltInRole.PROJECT_CREATOR,
                   settings.getProjectCreationSetting() == ProjectCreationSetting.EMPTY_PROJECT_CREATION_ALLOWED);
            toggle(signedInRoles, BuiltInRole.PROJECT_UPLOADER,
                   settings.getProjectUploadSetting() == ProjectUploadSetting.PROJECT_UPLOAD_ALLOWED);
            accessManager.setAssignedRoles(Subject.forAnySignedInUser(), application, signedInRoles);
        } finally {
            writeLock.unlock();
        }
    }

    private static void toggle(Set<RoleId> roles, BuiltInRole role, boolean assigned) {
        if (assigned) {
            roles.add(role.getRoleId());
        } else {
            roles.remove(role.getRoleId());
        }
    }
}
