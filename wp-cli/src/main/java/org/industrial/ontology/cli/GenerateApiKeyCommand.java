package org.industrial.ontology.cli;

import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

/**
 * {@code wp-cli generate-api-key --user <name> --purpose <text>} (docs/01 §8): the legacy {@code generate-api-key},
 * with options instead of prompts. The key is printed once and only its hash is stored; it authenticates as the
 * user with {@code Authorization: ApiKey <key>}.
 * <p>
 * The user need not have signed in yet, so that keys can be made for integration accounts that never sign in; the
 * command says so when the user has no {@code Users} record.
 */
@Component
@Command(name = "generate-api-key",
         mixinStandardHelpOptions = true,
         description = {
                 "Generates an API key for a user and prints it once.",
                 "Exit codes: 0 done, 1 failed, 2 wrong arguments."})
public class GenerateApiKeyCommand implements Callable<Integer> {

    private final ApiKeyService apiKeyService;

    private final UserService userService;

    @Spec
    private CommandSpec spec;

    @Option(names = "--user", required = true, description = "The user that the key authenticates as.")
    private String userName;

    @Option(names = "--purpose", required = true, description = "What the key is for, shown in the user's key list.")
    private String purpose;

    public GenerateApiKeyCommand(ApiKeyService apiKeyService, UserService userService) {
        this.apiKeyService = apiKeyService;
        this.userService = userService;
    }

    @Override
    public Integer call() {
        var userId = UserId.getUserId(userName.trim());
        try {
            var generated = apiKeyService.generateApiKeyForUser(userId, purpose);
            var out = spec.commandLine().getOut();
            if (userService.getUserProfile(userId).isEmpty()) {
                out.printf("Note: %s has not signed in yet; the key works anyway.%n", userId.getUserName());
            }
            out.printf("Generated API key %s for %s:%n%n", generated.details().apiKeyId().getId(),
                       userId.getUserName());
            out.printf("    %s%n%n", generated.apiKey().getKey());
            out.println("Please keep this API key safe. Treat it as a password.");
            out.println("You should NOT distribute it or store it in version control repositories.");
            out.println("This API key cannot be recovered. You will need to generate a new key if you lose this one.");
            out.flush();
            return 0;
        } catch (IllegalArgumentException | WpException e) {
            throw new ParameterException(spec.commandLine(), e.getMessage(), e);
        }
    }
}
