package org.industrial.ontology.app.access;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.sharing.PersonId;
import org.industrial.ontology.domain.sharing.ProjectSharingSettings;
import org.industrial.ontology.domain.sharing.SharingPermission;
import org.industrial.ontology.domain.sharing.SharingSetting;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_SHARING_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_ANY_USER_DETAILS;

/**
 * Who may access a project (docs/01 §5.1; stage S6): the legacy {@code ProjectSharingSettingsManagerImpl} with the
 * checks of the {@code Get/SetProjectSharingSettingsActionHandler}s, which both require
 * {@code EditSharingSettings}.
 * <p>
 * A sharing setting gives a user one of the four sharing roles ({@link SharingRoles}); link sharing gives one to
 * every signed-in user. Setting the sharing settings replaces every role assignment on the project, as in the
 * legacy manager: users that are not in the settings lose their roles, and so do roles that are not sharing roles.
 * Unlike the legacy manager, the project's owner always keeps {@code MANAGE}: settings that leave the owner out keep
 * it, and settings that give the owner less are refused, whoever sends them, so that neither a collaborator nor the
 * owner can lock the owner out of the project.
 */
public class SharingService {

    /**
     * A sharing setting names neither a user nor an e-mail address that is known, or names an e-mail address that
     * more than one user has.
     */
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    /**
     * The sharing settings give the project's owner less than {@code MANAGE}.
     */
    public static final String OWNER_ACCESS_REQUIRED = "OWNER_ACCESS_REQUIRED";

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final UserRecordRepository userRecordRepository;

    public SharingService(@Nonnull AccessManager accessManager,
                          @Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                          @Nonnull UserRecordRepository userRecordRepository) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.userRecordRepository = checkNotNull(userRecordRepository);
    }

    /**
     * The users who have a sharing role on the project, sorted by user name, and the link sharing permission.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project's sharing settings
     */
    @Nonnull
    public ProjectSharingSettings getSharingSettings(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        requireExistingProject(projectId);
        accessManager.require(caller, ProjectResource.of(projectId), EDIT_SHARING_SETTINGS);
        return readSharingSettings(projectId);
    }

    /**
     * Replaces the project's role assignments with the settings, and returns the settings as they are stored now.
     * <p>
     * Each setting names a user by user name; for a user named twice the first setting counts. A name without a user
     * record is accepted only if it is the owner or already has a role on the project, so that the settings can be
     * read and written back unchanged. Users that Keycloak knows but that have never signed in have no record yet:
     * they sign in once, or an administrator uses {@code wp-cli set-permissions}. The legacy manager skipped unknown
     * users without a word; here nothing is changed and the request fails.
     * <p>
     * As in the legacy manager, a setting may name a user by e-mail address instead, but only if the caller may view
     * any user's details ({@code ViewAnyUserDetails}, which administrators have): the stored settings show user names,
     * so anyone else could find out whose address it is, or whether it belongs to anyone. For other callers an e-mail
     * address is just a name that no user has. An address that more than one user has is refused.
     * <p>
     * The owner keeps {@code MANAGE} when the settings leave the owner out.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project's sharing settings
     * @throws WpException               {@value #USER_NOT_FOUND} (400) if a setting names an unknown user or an
     *                                   e-mail address of several users; {@value #OWNER_ACCESS_REQUIRED} (400) if the
     *                                   settings give the owner less than {@code MANAGE}
     */
    @Nonnull
    public ProjectSharingSettings setSharingSettings(@Nonnull UserId caller,
                                                     @Nonnull ProjectSharingSettings settings) {
        var projectId = settings.getProjectId();
        var owner = requireExistingProject(projectId).getOwner();
        var project = ProjectResource.of(projectId);
        accessManager.require(caller, project, EDIT_SHARING_SETTINGS);

        var currentSubjects = accessManager.getSubjectsWithAccessToResource(project);
        Set<String> currentUserNames = currentSubjects.stream()
                                                      .flatMap(subject -> subject.getUserName().stream())
                                                      .collect(toSet());
        var byEmailAddress = accessManager.hasPermission(Subject.forUser(caller),
                                                         ApplicationResource.get(),
                                                         VIEW_ANY_USER_DETAILS);
        Map<UserId, SharingPermission> userPermissions = new LinkedHashMap<>();
        List<String> unknownPersons = new ArrayList<>();
        List<String> sharedEmailAddresses = new ArrayList<>();
        for (SharingSetting setting : settings.getSharingSettings()) {
            var personId = setting.getPersonId().getId();
            var users = resolveUsers(personId, owner, currentUserNames, byEmailAddress);
            if (users.size() == 1) {
                userPermissions.putIfAbsent(users.get(0), setting.getSharingPermission());
            } else if (users.isEmpty()) {
                unknownPersons.add(personId);
            } else {
                sharedEmailAddresses.add(personId);
            }
        }
        if (!unknownPersons.isEmpty() || !sharedEmailAddresses.isEmpty()) {
            throw new WpException(USER_NOT_FOUND, 400, unknownUsersMessage(unknownPersons,
                                                                           sharedEmailAddresses,
                                                                           byEmailAddress));
        }
        var ownerPermission = userPermissions.putIfAbsent(owner, SharingPermission.MANAGE);
        if (ownerPermission != null && ownerPermission != SharingPermission.MANAGE) {
            throw new WpException(OWNER_ACCESS_REQUIRED, 400, "The owner of the project, " + owner.getUserName()
                    + ", keeps MANAGE");
        }

        // The new assignments first and the removals last, so that users who keep their access never lose it.
        accessManager.setAssignedRoles(Subject.forAnySignedInUser(),
                                       project,
                                       toRoles(settings.getLinkSharingPermission()));
        userPermissions.forEach((userId, permission) -> accessManager.setAssignedRoles(
                Subject.forUser(userId),
                project,
                SharingRoles.fromSharingPermission(permission)));
        for (Subject subject : currentSubjects) {
            var keeps = subject.isAnySignedInUser()
                    || subject.getUserId().map(userPermissions::containsKey).orElse(false);
            if (!keeps) {
                accessManager.setAssignedRoles(subject, project, Set.of());
            }
        }
        return readSharingSettings(projectId);
    }

    /**
     * Gives the user exactly the sharing role of the permission on the project, replacing the user's other roles
     * there ({@code wp-cli set-permissions}, as the legacy {@code SetPermissions} tool). The user need not have
     * signed in yet. Unlike the legacy tool, an unknown project is refused. This is the operator's tool, so it may
     * change the owner's role too.
     *
     * @throws ProjectNotFoundException if the project does not exist
     * @throws IllegalArgumentException if the user is the guest user
     */
    public void setUserPermission(@Nonnull ProjectId projectId,
                                  @Nonnull UserId userId,
                                  @Nonnull SharingPermission permission) {
        requireExistingProject(projectId);
        checkArgument(!userId.isGuest(), "Roles cannot be given to the guest user");
        accessManager.setAssignedRoles(Subject.forUser(userId),
                                       ProjectResource.of(projectId),
                                       SharingRoles.fromSharingPermission(permission));
    }

    private ProjectSharingSettings readSharingSettings(ProjectId projectId) {
        var project = ProjectResource.of(projectId);
        var sharingSettings = accessManager.getSubjectsWithAccessToResource(project)
                                           .stream()
                                           .filter(subject -> !subject.isGuest())
                                           .flatMap(subject -> subject.getUserId().stream())
                                           .distinct()
                                           .flatMap(userId -> SharingRoles.toSharingPermission(
                                                                                  accessManager.getAssignedRoles(
                                                                                          Subject.forUser(userId),
                                                                                          project))
                                                                          .map(permission -> new SharingSetting(
                                                                                  PersonId.of(userId),
                                                                                  permission))
                                                                          .stream())
                                           .sorted()
                                           .toList();
        var linkSharing = SharingRoles.toSharingPermission(
                accessManager.getAssignedRoles(Subject.forAnySignedInUser(), project));
        return new ProjectSharingSettings(projectId, linkSharing, sharingSettings);
    }

    /**
     * The users that the person id names: a user name with a record, the owner, a user name that already has a role
     * on the project, or, if {@code byEmailAddress}, the users whose e-mail address it is (as the legacy
     * {@code getUserByUserIdOrEmail}, which took the first of them; at most two are looked up here).
     */
    private List<UserId> resolveUsers(String personId,
                                      UserId owner,
                                      Set<String> currentUserNames,
                                      boolean byEmailAddress) {
        if (personId.isBlank()) {
            return List.of();
        }
        var userId = UserId.getUserId(personId);
        if (userId.isGuest()) {
            return List.of();
        }
        if (userId.equals(owner) || userRecordRepository.findOne(userId).isPresent()) {
            return List.of(userId);
        }
        if (byEmailAddress) {
            var users = userRecordRepository.findByEmailAddress(personId, 2)
                                            .stream()
                                            .map(UserRecordDocument::getUserId)
                                            .toList();
            if (!users.isEmpty()) {
                return users;
            }
        }
        return currentUserNames.contains(personId) ? List.of(userId) : List.of();
    }

    private static String unknownUsersMessage(List<String> unknownPersons,
                                              List<String> sharedEmailAddresses,
                                              boolean byEmailAddress) {
        var problems = new ArrayList<String>();
        if (!unknownPersons.isEmpty()) {
            problems.add((byEmailAddress ? "No user has the name or e-mail address " : "No user has the name ")
                                 + String.join(", ", unknownPersons));
        }
        if (!sharedEmailAddresses.isEmpty()) {
            problems.add("More than one user has the e-mail address " + String.join(", ", sharedEmailAddresses));
        }
        return String.join("; ", problems);
    }

    private static Collection<RoleId> toRoles(Optional<SharingPermission> permission) {
        return permission.<Collection<RoleId>>map(SharingRoles::fromSharingPermission).orElse(Set.of());
    }

    private ProjectDetails requireExistingProject(ProjectId projectId) {
        return projectDetailsRepository.findOne(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
