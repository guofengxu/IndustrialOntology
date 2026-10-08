package org.industrial.ontology.app.admin.persistence;

import org.industrial.ontology.domain.app.ApplicationLocation;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The single document of the {@code ApplicationPreferences} collection, as Morphia wrote the legacy
 * {@code ApplicationPreferences}: {@code {_id: "Preferences", applicationName, systemNotificationEmailAddress,
 * applicationLocation: {scheme, host, path, port}, maxUploadSize}}. The account, project-creation and upload settings
 * of the application are role assignments, not preferences (legacy {@code ApplicationSettingsManager}).
 * <p>
 * Like the legacy {@code @AlsoLoad("adminEmailAddress")}, a document from before the field was renamed is read from
 * {@code adminEmailAddress}; writing it back stores {@code systemNotificationEmailAddress}.
 *
 * @param id                  always {@link #ID}
 * @param legacyAdminEmailAddress the pre-rename field; read, never written
 */
@Document(ApplicationPreferencesDocument.COLLECTION)
public record ApplicationPreferencesDocument(@Id @Nonnull String id,
                                             @Nonnull String applicationName,
                                             @Nonnull String systemNotificationEmailAddress,
                                             @Nonnull Location applicationLocation,
                                             long maxUploadSize,
                                             @ReadOnlyProperty @org.springframework.data.mongodb.core.mapping.Field(
                                                     "adminEmailAddress") @Nullable String legacyAdminEmailAddress) {

    public static final String COLLECTION = "ApplicationPreferences";

    public static final String ID = "Preferences";

    public ApplicationPreferencesDocument {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(applicationLocation, "applicationLocation");
        id = ID;
        if (systemNotificationEmailAddress == null) {
            systemNotificationEmailAddress = Objects.requireNonNullElse(legacyAdminEmailAddress, "");
        }
        legacyAdminEmailAddress = null;
    }

    @Nonnull
    public static ApplicationPreferencesDocument of(@Nonnull String applicationName,
                                                    @Nonnull String systemNotificationEmailAddress,
                                                    @Nonnull ApplicationLocation applicationLocation,
                                                    long maxUploadSize) {
        return new ApplicationPreferencesDocument(ID, applicationName, systemNotificationEmailAddress,
                                                  Location.of(applicationLocation), maxUploadSize, null);
    }

    /**
     * The defaults that the legacy server stored when the collection was empty
     * ({@code DefaultApplicationPreferences}).
     */
    @Nonnull
    public static ApplicationPreferencesDocument defaults() {
        return of("WebProtégé", "", new ApplicationLocation("https", "", "", 443), Long.MAX_VALUE);
    }

    /**
     * The embedded {@code applicationLocation}, as Morphia wrote the legacy {@link ApplicationLocation}.
     */
    public record Location(@Nonnull String scheme, @Nonnull String host, @Nonnull String path, int port) {

        public Location {
            Objects.requireNonNull(scheme, "scheme");
            Objects.requireNonNull(host, "host");
            Objects.requireNonNull(path, "path");
        }

        @Nonnull
        public static Location of(@Nonnull ApplicationLocation location) {
            return new Location(location.getScheme(), location.getHost(), location.getPath(), location.getPort());
        }

        @Nonnull
        public ApplicationLocation toApplicationLocation() {
            return new ApplicationLocation(scheme, host, path, port);
        }
    }
}
