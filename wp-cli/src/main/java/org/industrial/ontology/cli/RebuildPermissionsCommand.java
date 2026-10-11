package org.industrial.ontology.cli;

import org.industrial.ontology.app.access.PermissionService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

/**
 * {@code wp-cli rebuild-permissions} (docs/01 §8), as the legacy {@code RebuildPermissions} tool: recomputes the
 * stored role and action closures of every role assignment from its assigned roles, for when the built-in roles have
 * changed. {@code POST /api/v1/admin/permissions/rebuild} does the same on a running server.
 */
@Component
@Command(name = "rebuild-permissions",
         mixinStandardHelpOptions = true,
         description = {
                 "Recomputes the stored permissions of every role assignment from its roles.",
                 "Exit codes: 0 done, 1 failed."})
public class RebuildPermissionsCommand implements Callable<Integer> {

    private final PermissionService permissionService;

    @Spec
    private CommandSpec spec;

    public RebuildPermissionsCommand(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public Integer call() {
        var out = spec.commandLine().getOut();
        out.println("Rebuilding permissions...");
        out.flush();
        permissionService.rebuildPermissions();
        out.println("Finished rebuilding permissions");
        out.flush();
        return 0;
    }
}
