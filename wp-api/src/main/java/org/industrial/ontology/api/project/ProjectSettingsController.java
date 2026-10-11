package org.industrial.ontology.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectLanguages;
import org.industrial.ontology.app.project.ProjectSettingsService;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.IRIPrefixUpdateStrategy;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.LangTag;
import org.industrial.ontology.domain.projectsettings.ProjectSettings;
import org.industrial.ontology.domain.projectsettings.WebhookSetting;
import org.industrial.ontology.domain.projectsettings.WebhookSettings;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A project's settings (docs/02 §3): name, description and webhooks, languages, prefixes and how new entities get
 * their IRIs. The services check the permissions.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Project settings", description = "Settings, languages, prefixes, new entity settings and webhooks")
public class ProjectSettingsController {

    private final ProjectSettingsService settingsService;

    public ProjectSettingsController(ProjectSettingsService settingsService) {
        this.settingsService = checkNotNull(settingsService);
    }

    @Operation(summary = "The project's settings, webhooks included (permission EditProjectSettings)")
    @GetMapping("/projects/{projectId}/settings")
    public ProjectSettingsDto settings(@Caller UserId caller, @PathVariable String projectId) {
        return ProjectSettingsDto.of(settingsService.getProjectSettings(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Replaces the project's settings (permission EditProjectSettings)",
               description = "The webhooks are replaced too; each must be an http or https URL.")
    @PutMapping("/projects/{projectId}/settings")
    public ProjectSettingsDto setSettings(@Caller UserId caller,
                                          @PathVariable String projectId,
                                          @RequestBody ProjectSettingsDto settings) {
        if (settings.displayName() == null || settings.defaultLanguage() == null
                || settings.defaultDisplayNameSettings() == null || settings.webhooks() == null) {
            throw WpException.invalidRequest("The display name, languages and webhooks need values");
        }
        var stored = settingsService.setProjectSettings(caller, ProjectSettings.get(
                ProjectIds.parse(projectId),
                settings.displayName(),
                Objects.requireNonNullElse(settings.description(), ""),
                settings.defaultLanguage(),
                settings.defaultDisplayNameSettings(),
                WebhookSettings.get(toWebhookSettings(settings.webhooks()))));
        return ProjectSettingsDto.of(stored);
    }

    @Operation(summary = "The project's webhooks (permission EditProjectSettings)")
    @GetMapping("/projects/{projectId}/webhooks")
    public List<WebhookDto> webhooks(@Caller UserId caller, @PathVariable String projectId) {
        return settingsService.getWebhooks(caller, ProjectIds.parse(projectId)).stream().map(WebhookDto::of).toList();
    }

    @Operation(summary = "Replaces the project's webhooks (permission EditProjectSettings)")
    @PutMapping("/projects/{projectId}/webhooks")
    public List<WebhookDto> setWebhooks(@Caller UserId caller,
                                        @PathVariable String projectId,
                                        @RequestBody List<WebhookDto> webhooks) {
        return settingsService.setWebhooks(caller, ProjectIds.parse(projectId), toWebhookSettings(webhooks))
                              .stream()
                              .map(WebhookDto::of)
                              .toList();
    }

    @Operation(summary = "The project's languages and the languages its annotations use (permission ViewProject)")
    @GetMapping("/projects/{projectId}/languages")
    public LanguagesDto languages(@Caller UserId caller, @PathVariable String projectId) {
        return LanguagesDto.of(settingsService.getProjectLanguages(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Sets the project's default language and display name languages "
            + "(permission EditProjectSettings)")
    @PutMapping("/projects/{projectId}/languages")
    public LanguagesDto setLanguages(@Caller UserId caller,
                                     @PathVariable String projectId,
                                     @RequestBody LanguagesRequest request) {
        if (request.defaultLanguage() == null || request.displayNameSettings() == null) {
            throw WpException.invalidRequest("The default language and the display name settings need values");
        }
        return LanguagesDto.of(settingsService.setProjectLanguages(caller,
                                                                   ProjectIds.parse(projectId),
                                                                   request.defaultLanguage(),
                                                                   request.displayNameSettings()));
    }

    @Operation(summary = "The language tags that the project's annotations use (permission ViewProject)")
    @GetMapping("/projects/{projectId}/lang-tags")
    public List<String> langTags(@Caller UserId caller, @PathVariable String projectId) {
        return settingsService.getLangTags(caller, ProjectIds.parse(projectId))
                              .stream()
                              .map(LangTag::format)
                              .toList();
    }

    @Operation(summary = "The project's prefix declarations (permission ViewProject)")
    @GetMapping("/projects/{projectId}/prefixes")
    public PrefixesDto prefixes(@Caller UserId caller, @PathVariable String projectId) {
        return new PrefixesDto(settingsService.getPrefixDeclarations(caller, ProjectIds.parse(projectId))
                                              .getPrefixes());
    }

    @Operation(summary = "Replaces the project's prefix declarations (permission EditProjectPrefixes)",
               description = "Prefix names end with a colon, for example {\"prefixes\": {\"ex:\": \"http://…#\"}}.")
    @PutMapping("/projects/{projectId}/prefixes")
    public PrefixesDto setPrefixes(@Caller UserId caller,
                                   @PathVariable String projectId,
                                   @RequestBody PrefixesDto prefixes) {
        if (prefixes.prefixes() == null) {
            throw WpException.invalidRequest("prefixes needs a value");
        }
        return new PrefixesDto(settingsService.setPrefixDeclarations(caller,
                                                                     ProjectIds.parse(projectId),
                                                                     prefixes.prefixes())
                                              .getPrefixes());
    }

    @Operation(summary = "How new entities get their IRIs (permission ViewProject)")
    @GetMapping("/projects/{projectId}/crud-settings")
    public EntityCrudKitSettings<?> crudSettings(@Caller UserId caller, @PathVariable String projectId) {
        return settingsService.getEntityCrudKitSettings(caller, ProjectIds.parse(projectId));
    }

    @Operation(summary = "Sets how new entities get their IRIs (permission EditNewEntitySettings)",
               description = "prefixUpdateStrategy=FIND_AND_REPLACE also renames the entities whose IRIs start with "
                       + "the old prefix (permission EditOntology too).")
    @PutMapping("/projects/{projectId}/crud-settings")
    public EntityCrudKitSettings<?> setCrudSettings(
            @Caller UserId caller,
            @PathVariable String projectId,
            @RequestParam(defaultValue = "LEAVE_INTACT") IRIPrefixUpdateStrategy prefixUpdateStrategy,
            @RequestBody CrudSettingsRequest settings) {
        if (settings.prefixSettings() == null || settings.suffixSettings() == null) {
            throw WpException.invalidRequest("prefixSettings and suffixSettings need values");
        }
        return settingsService.setEntityCrudKitSettings(caller,
                                                        ProjectIds.parse(projectId),
                                                        EntityCrudKitSettings.get(settings.prefixSettings(),
                                                                                  settings.suffixSettings()),
                                                        prefixUpdateStrategy);
    }

    @Operation(summary = "The kits that give new entities their IRI suffixes")
    @GetMapping("/crud-kits")
    public List<CrudKitDto> crudKits(@Caller UserId caller) {
        return settingsService.getEntityCrudKits(caller).stream().map(CrudKitDto::of).toList();
    }

    private static List<WebhookSetting> toWebhookSettings(List<WebhookDto> webhooks) {
        return webhooks.stream()
                       .map(webhook -> {
                           if (webhook == null || webhook.payloadUrl() == null) {
                               throw WpException.invalidRequest("A webhook needs a payloadUrl");
                           }
                           return WebhookSetting.get(webhook.payloadUrl(),
                                                     Set.copyOf(Objects.requireNonNullElse(webhook.eventTypes(),
                                                                                           List.of())));
                       })
                       .toList();
    }

    /**
     * The legacy {@code ProjectSettings} without its project id, which the path gives, and without Slack.
     */
    public record ProjectSettingsDto(String displayName,
                                     String description,
                                     DictionaryLanguage defaultLanguage,
                                     DisplayNameSettings defaultDisplayNameSettings,
                                     List<WebhookDto> webhooks) {

        static ProjectSettingsDto of(ProjectSettings settings) {
            return new ProjectSettingsDto(settings.getProjectDisplayName(),
                                          settings.getProjectDescription(),
                                          settings.getDefaultLanguage(),
                                          settings.getDefaultDisplayNameSettings(),
                                          settings.getWebhookSettings()
                                                  .getWebhookSettings()
                                                  .stream()
                                                  .map(WebhookDto::of)
                                                  .toList());
        }
    }

    /**
     * A webhook: the URL that the server posts to and the events it posts.
     */
    public record WebhookDto(String payloadUrl, List<ProjectWebhookEventType> eventTypes) {

        static WebhookDto of(WebhookSetting setting) {
            return new WebhookDto(setting.getPayloadUrl(), setting.getEventTypes().stream().sorted().toList());
        }
    }

    /**
     * The project's languages; {@code languageUsage} lists the languages in use, most used first.
     */
    public record LanguagesDto(DictionaryLanguage defaultLanguage,
                               DisplayNameSettings displayNameSettings,
                               List<LanguageUsageDto> languageUsage) {

        static LanguagesDto of(ProjectLanguages languages) {
            return new LanguagesDto(languages.defaultLanguage(),
                                    languages.displayNameSettings(),
                                    languages.languageUsage()
                                             .stream()
                                             .map(usage -> new LanguageUsageDto(usage.dictionaryLanguage(),
                                                                                usage.referenceCount()))
                                             .toList());
        }
    }

    public record LanguageUsageDto(DictionaryLanguage language, int referenceCount) {
    }

    public record LanguagesRequest(DictionaryLanguage defaultLanguage, DisplayNameSettings displayNameSettings) {
    }

    /**
     * Prefix names, each ending with a colon, to prefixes.
     */
    public record PrefixesDto(Map<String, String> prefixes) {
    }

    /**
     * The legacy {@code EntityCrudKitSettings} as written: the suffix settings name their kit in {@code _class}. It is
     * not bound as {@code EntityCrudKitSettings<?>}, whose wildcard would leave Jackson without the suffix settings'
     * type.
     */
    public record CrudSettingsRequest(EntityCrudKitPrefixSettings prefixSettings,
                                      EntityCrudKitSuffixSettings suffixSettings) {
    }

    /**
     * An IRI suffix kit, with the settings that a project gets when it chooses the kit.
     */
    public record CrudKitDto(String kitId,
                             String displayName,
                             EntityCrudKitPrefixSettings defaultPrefixSettings,
                             EntityCrudKitSuffixSettings defaultSuffixSettings) {

        static CrudKitDto of(EntityCrudKit<?> kit) {
            return new CrudKitDto(kit.getKitId().getLexicalForm(),
                                  kit.getDisplayName(),
                                  kit.getDefaultPrefixSettings(),
                                  kit.getDefaultSuffixSettings());
        }
    }
}
