package org.industrial.ontology.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.access.SharingService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.sharing.PersonId;
import org.industrial.ontology.domain.sharing.ProjectSharingSettings;
import org.industrial.ontology.domain.sharing.SharingPermission;
import org.industrial.ontology.domain.sharing.SharingSetting;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Who may access a project (docs/02 §3 {@code /sharing}). Reading and writing both need
 * {@code EditSharingSettings}, as in the legacy handlers; the service checks it.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/sharing")
@Tag(name = "Sharing", description = "The users who may access a project, and link sharing")
public class ProjectSharingController {

    private final SharingService sharingService;

    public ProjectSharingController(SharingService sharingService) {
        this.sharingService = checkNotNull(sharingService);
    }

    @Operation(summary = "The project's sharing settings (permission EditSharingSettings)")
    @GetMapping
    public SharingDto sharing(@Caller UserId caller, @PathVariable String projectId) {
        return SharingDto.of(sharingService.getSharingSettings(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Replaces the project's sharing settings (permission EditSharingSettings)",
               description = "Every role assignment on the project is replaced: users that are not listed lose their "
                       + "access, except the owner, who always keeps MANAGE (giving the owner less is a 400 "
                       + "OWNER_ACCESS_REQUIRED). A user is named by user name and must have signed in at least "
                       + "once, unless the user already has access; callers with ViewAnyUserDetails may name a user "
                       + "by e-mail address. linkSharing gives every signed-in user a permission; NONE gives none.")
    @PutMapping
    public SharingDto setSharing(@Caller UserId caller,
                                 @PathVariable String projectId,
                                 @RequestBody SharingDto sharing) {
        if (sharing.sharingSettings() == null) {
            throw WpException.invalidRequest("sharingSettings needs a value");
        }
        var settings = sharing.sharingSettings()
                              .stream()
                              .map(setting -> {
                                  if (setting == null || setting.userId() == null || setting.permission() == null) {
                                      throw WpException.invalidRequest("Each sharing setting needs a userId and "
                                                                               + "a permission");
                                  }
                                  return new SharingSetting(new PersonId(setting.userId()), setting.permission());
                              })
                              .toList();
        var linkSharing = sharing.linkSharing() == null ? LinkSharing.NONE : sharing.linkSharing();
        var stored = sharingService.setSharingSettings(caller,
                                                       new ProjectSharingSettings(ProjectIds.parse(projectId),
                                                                                  linkSharing.toPermission(),
                                                                                  settings));
        return SharingDto.of(stored);
    }

    /**
     * {@code sharingSettings} are sorted by user name.
     */
    public record SharingDto(List<SharingSettingDto> sharingSettings, LinkSharing linkSharing) {

        static SharingDto of(ProjectSharingSettings settings) {
            return new SharingDto(settings.getSharingSettings()
                                          .stream()
                                          .map(setting -> new SharingSettingDto(setting.getPersonId().getId(),
                                                                                setting.getSharingPermission()))
                                          .toList(),
                                  LinkSharing.of(settings.getLinkSharingPermission()));
        }
    }

    /**
     * A user and the permission it is given; when an administrator writes, {@code userId} may also be an e-mail
     * address.
     */
    public record SharingSettingDto(String userId, SharingPermission permission) {
    }

    /**
     * What every signed-in user may do: nothing, or one of the sharing permissions (the legacy client offered all
     * four).
     */
    public enum LinkSharing {

        NONE, VIEW, COMMENT, EDIT, MANAGE;

        static LinkSharing of(Optional<SharingPermission> permission) {
            return permission.map(p -> LinkSharing.valueOf(p.name())).orElse(NONE);
        }

        Optional<SharingPermission> toPermission() {
            return this == NONE ? Optional.empty() : Optional.of(SharingPermission.valueOf(name()));
        }
    }
}
