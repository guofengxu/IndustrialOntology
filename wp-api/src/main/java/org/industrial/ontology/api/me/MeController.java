package org.industrial.ontology.api.me;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.apikey.ApiKeyDetails;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.ApiKeyId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The signed-in user and its API keys (docs/02 §2). Any signed-in user may use these, for itself only.
 */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me", description = "The signed-in user and its API keys")
public class MeController {

    private final UserService userService;

    private final ApiKeyService apiKeyService;

    public MeController(UserService userService, ApiKeyService apiKeyService) {
        this.userService = checkNotNull(userService);
        this.apiKeyService = checkNotNull(apiKeyService);
    }

    @Operation(summary = "The signed-in user, with the actions it may perform on the application",
               description = "Replaces the legacy userInSession. applicationActions are action ids such as "
                       + "EditApplicationSettings; the client shows the administration pages by them.")
    @GetMapping
    public MeDto me(@Caller UserId caller) {
        var me = userService.getCurrentUser(caller);
        return new MeDto(me.userId().getUserName(), me.displayName(), me.emailAddress(),
                         me.applicationActions().stream().map(ActionId::getId).toList());
    }

    @Operation(summary = "The API keys of the signed-in user, without the keys themselves")
    @GetMapping("/api-keys")
    public List<ApiKeyDto> apiKeys(@Caller UserId caller) {
        return apiKeyService.getApiKeys(caller).stream().map(ApiKeyDto::of).toList();
    }

    @Operation(summary = "Generates an API key for the signed-in user",
               description = "The response is the only time the key is shown; send it as 'Authorization: ApiKey "
                       + "<key>'.")
    @PostMapping("/api-keys")
    public ResponseEntity<NewApiKeyDto> generateApiKey(@Caller UserId caller, @RequestBody NewApiKeyRequest request) {
        var generated = apiKeyService.generateApiKey(caller, Objects.requireNonNullElse(request.purpose(), ""));
        var details = generated.details();
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                                                  .path("/{apiKeyId}")
                                                  .buildAndExpand(details.apiKeyId().getId())
                                                  .toUri();
        return ResponseEntity.created(location)
                             .cacheControl(CacheControl.noStore())
                             .body(new NewApiKeyDto(details.apiKeyId().getId(), generated.apiKey().getKey(),
                                                    details.purpose(), details.createdAt()));
    }

    @Operation(summary = "Revokes one of the signed-in user's API keys")
    @DeleteMapping("/api-keys/{apiKeyId}")
    public ResponseEntity<Void> revokeApiKey(@Caller UserId caller, @PathVariable String apiKeyId) {
        apiKeyService.revokeApiKey(caller, ApiKeyId.valueOf(apiKeyId));
        return ResponseEntity.noContent().build();
    }

    /**
     * {@code GET /api/v1/me}.
     *
     * @param email {@code null} when the address is not known
     */
    public record MeDto(String userId, String displayName, String email, List<String> applicationActions) {
    }

    public record ApiKeyDto(String apiKeyId, String purpose, Instant createdAt) {

        static ApiKeyDto of(ApiKeyDetails details) {
            return new ApiKeyDto(details.apiKeyId().getId(), details.purpose(), details.createdAt());
        }
    }

    public record NewApiKeyRequest(String purpose) {
    }

    /**
     * A key that was just generated, with the key itself.
     */
    public record NewApiKeyDto(String apiKeyId, String apiKey, String purpose, Instant createdAt) {
    }
}
