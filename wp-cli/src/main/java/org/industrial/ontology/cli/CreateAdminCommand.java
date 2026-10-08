package org.industrial.ontology.cli;

import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * {@code wp-cli create-admin --user <name> --email <address> [--password]} (docs/01 §8): makes a user an
 * administrator, replacing the legacy {@code create-admin-account}. It writes the {@code Users} record and adds
 * {@code SystemAdmin} to the user's application roles ({@link UserService#createAdministrator}).
 * <p>
 * Administrators normally sign in with Keycloak, under the same user name. {@code --password} sets a local password
 * for the fallback login ({@code webprotege.auth.local-login.enabled}); without a value it is asked for and not echoed,
 * so that it stays out of the shell history.
 */
@Component
@Command(name = "create-admin",
         mixinStandardHelpOptions = true,
         description = {
                 "Makes a user an administrator (SystemAdmin), creating the user if needed.",
                 "Exit codes: 0 done, 1 failed, 2 wrong arguments."})
public class CreateAdminCommand implements Callable<Integer> {

    private final UserService userService;

    @Spec
    private CommandSpec spec;

    @Option(names = "--user", required = true, description = "The user name, as in Keycloak.")
    private String userName;

    @Option(names = "--email", required = true, description = "The e-mail address of the user.")
    private String emailAddress;

    @Option(names = "--password", arity = "0..1", interactive = true,
            description = "A local password for the fallback login, at least 12 characters. Asked for when no value is "
                    + "given; a value on the command line is visible to other users of the machine.")
    private char[] password;

    public CreateAdminCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Integer call() {
        var userId = UserId.getUserId(userName.trim());
        try {
            var account = userService.createAdministrator(userId, emailAddress.trim(),
                                                          password == null ? null : new String(password));
            var out = spec.commandLine().getOut();
            out.printf("%s %s, who is now an administrator (SystemAdmin).%n",
                       account.created() ? "Created user" : "Updated user", userId.getUserName());
            if (account.passwordSet()) {
                out.println("The local password is set; it works where webprotege.auth.local-login.enabled is "
                                    + "true.");
            }
            out.flush();
            return 0;
        } catch (IllegalArgumentException e) {
            throw new ParameterException(spec.commandLine(), e.getMessage(), e);
        } finally {
            if (password != null) {
                Arrays.fill(password, ' ');
            }
        }
    }
}
