package org.industrial.ontology.cli;

import org.industrial.ontology.app.access.SharingService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.sharing.SharingPermission;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

/**
 * {@code wp-cli set-permissions} (docs/01 §8): gives a user one of the four sharing roles on a project, as the legacy
 * interactive {@code SetPermissions} tool did, with options instead of prompts. The role replaces the user's other
 * roles on the project and applies at once, since permissions are not cached (docs/01 §5.1).
 */
@Component
@Command(name = "set-permissions",
         mixinStandardHelpOptions = true,
         description = {
                 "Gives a user a role on a project, replacing the user's other roles on it.",
                 "Exit codes: 0 done, 1 failed, 2 wrong arguments."})
public class SetPermissionsCommand implements Callable<Integer> {

    /**
     * The roles of the legacy tool's menu.
     */
    enum ProjectRole {

        CAN_VIEW(SharingPermission.VIEW),

        CAN_COMMENT(SharingPermission.COMMENT),

        CAN_EDIT(SharingPermission.EDIT),

        CAN_MANAGE(SharingPermission.MANAGE);

        private final SharingPermission permission;

        ProjectRole(SharingPermission permission) {
            this.permission = permission;
        }
    }

    private final SharingService sharingService;

    private final UserService userService;

    @Spec
    private CommandSpec spec;

    @Option(names = "--project", required = true, description = "The project id.")
    private String projectId;

    @Option(names = "--user", required = true, description = "The user that gets the role.")
    private String userName;

    @Option(names = "--role", required = true,
            description = "One of ${COMPLETION-CANDIDATES}.")
    private ProjectRole role;

    public SetPermissionsCommand(SharingService sharingService, UserService userService) {
        this.sharingService = sharingService;
        this.userService = userService;
    }

    @Override
    public Integer call() {
        if (!ProjectId.isWelFormedProjectId(projectId.trim())) {
            throw new ParameterException(spec.commandLine(), "--project needs a project id (a UUID)");
        }
        var project = ProjectId.get(projectId.trim());
        var userId = UserId.getUserId(userName.trim());
        try {
            sharingService.setUserPermission(project, userId, role.permission);
        } catch (IllegalArgumentException | WpException e) {
            throw new ParameterException(spec.commandLine(), e.getMessage(), e);
        }
        var out = spec.commandLine().getOut();
        if (userService.getUserProfile(userId).isEmpty()) {
            out.printf("Note: %s has not signed in yet; the role applies once they do.%n", userId.getUserName());
        }
        out.printf("%s now has %s on project %s.%n", userId.getUserName(), role, project.getId());
        out.flush();
        return 0;
    }
}
