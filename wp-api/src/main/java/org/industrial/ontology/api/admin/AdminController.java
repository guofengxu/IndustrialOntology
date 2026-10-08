package org.industrial.ontology.api.admin;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.admin.ApplicationSettingsService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.app.AccountCreationSetting;
import org.industrial.ontology.domain.app.ApplicationLocation;
import org.industrial.ontology.domain.app.ApplicationSettings;
import org.industrial.ontology.domain.app.NotificationEmailsSetting;
import org.industrial.ontology.domain.app.ProjectCreationSetting;
import org.industrial.ontology.domain.app.ProjectUploadSetting;
import org.industrial.ontology.domain.core.EmailAddress;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Application administration (docs/02 §2). The services check the permissions: {@code EditApplicationSettings} for
 * the settings, {@code ViewAnyUserDetails} for the user list; both come with {@code SystemAdmin}.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administration", description = "Application settings and users")
public class AdminController {

    private final ApplicationSettingsService applicationSettingsService;

    private final UserService userService;

    public AdminController(ApplicationSettingsService applicationSettingsService, UserService userService) {
        this.applicationSettingsService = checkNotNull(applicationSettingsService);
        this.userService = checkNotNull(userService);
    }

    @Operation(summary = "The application settings (permission EditApplicationSettings)")
    @GetMapping("/settings")
    public ApplicationSettingsDto settings(@Caller UserId caller) {
        return ApplicationSettingsDto.of(applicationSettingsService.getApplicationSettings(caller));
    }

    @Operation(summary = "Replaces the application settings (permission EditApplicationSettings)")
    @PutMapping("/settings")
    public ApplicationSettingsDto setSettings(@Caller UserId caller, @RequestBody ApplicationSettingsDto settings) {
        applicationSettingsService.setApplicationSettings(caller, settings.toApplicationSettings());
        return ApplicationSettingsDto.of(applicationSettingsService.getApplicationSettings(caller));
    }

    @Operation(summary = "The users whose name contains q, ignoring case (permission ViewAnyUserDetails)")
    @GetMapping("/users")
    public List<UserDto> users(@Caller UserId caller,
                               @RequestParam(defaultValue = "") String q,
                               @RequestParam(defaultValue = "50") int limit) {
        return userService.findUsers(caller, q, limit)
                          .stream()
                          .map(user -> new UserDto(user.userId().getUserName(), user.displayName(),
                                                   user.emailAddress()))
                          .toList();
    }

    /**
     * A user of the list; {@code email} is {@code null} when the address is not known.
     */
    public record UserDto(String userId, String displayName, String email) {
    }

    /**
     * The legacy {@code ApplicationSettings} without its account, project creator and uploader lists, which the
     * legacy server always left empty.
     */
    public record ApplicationSettingsDto(String applicationName,
                                         String systemNotificationEmailAddress,
                                         LocationDto applicationLocation,
                                         AccountCreationSetting accountCreationSetting,
                                         ProjectCreationSetting projectCreationSetting,
                                         ProjectUploadSetting projectUploadSetting,
                                         NotificationEmailsSetting notificationEmailsSetting,
                                         long maxUploadSize) {

        static ApplicationSettingsDto of(ApplicationSettings settings) {
            var location = settings.getApplicationLocation();
            return new ApplicationSettingsDto(settings.getApplicationName(),
                                              settings.getSystemNotificationEmailAddress().getEmailAddress(),
                                              new LocationDto(location.getScheme(), location.getHost(),
                                                              location.getPath(), location.getPort()),
                                              settings.getAccountCreationSetting(),
                                              settings.getProjectCreationSetting(),
                                              settings.getProjectUploadSetting(),
                                              settings.getNotificationEmailsSetting(),
                                              settings.getMaxUploadSize());
        }

        ApplicationSettings toApplicationSettings() {
            var location = applicationLocation;
            if (Stream.of(applicationName, systemNotificationEmailAddress, location, accountCreationSetting,
                          projectCreationSetting, projectUploadSetting, notificationEmailsSetting)
                      .anyMatch(value -> value == null)
                    || Stream.of(location.scheme(), location.host(), location.path()).anyMatch(value -> value == null)) {
                throw WpException.invalidRequest("Every application setting needs a value");
            }
            return new ApplicationSettings(applicationName,
                                           new EmailAddress(systemNotificationEmailAddress),
                                           new ApplicationLocation(location.scheme(), location.host(),
                                                                   location.path(), location.port()),
                                           accountCreationSetting,
                                           ImmutableList.of(),
                                           projectCreationSetting,
                                           ImmutableList.of(),
                                           projectUploadSetting,
                                           ImmutableList.of(),
                                           notificationEmailsSetting,
                                           maxUploadSize);
        }
    }

    public record LocationDto(String scheme, String host, String path, int port) {
    }
}
