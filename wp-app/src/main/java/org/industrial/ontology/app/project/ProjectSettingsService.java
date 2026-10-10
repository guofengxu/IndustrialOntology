package org.industrial.ontology.app.project;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.webhook.persistence.WebhookRepository;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.IRIPrefixUpdateStrategy;
import org.industrial.ontology.domain.crud.oboid.OBOIdSuffixKit;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixKit;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixKit;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.LangTag;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.projectsettings.ProjectSettings;
import org.industrial.ontology.domain.projectsettings.WebhookSetting;
import org.industrial.ontology.domain.projectsettings.WebhookSettings;
import org.industrial.ontology.domain.webhook.ProjectWebhook;
import org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettings;
import org.industrial.ontology.kernel.change.FindAndReplaceIRIPrefixChangeGeneratorFactory;
import org.industrial.ontology.kernel.entity.EntityRenamer;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_NEW_ENTITY_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_PROJECT_PREFIXES;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_PROJECT_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * The settings of a project (docs/01 §5.1; stage S6): the legacy {@code Get/SetProjectSettings},
 * {@code Get/SetProjectPrefixDeclarations}, {@code GetProjectInfo}, {@code GetProjectLangTags},
 * {@code Get/SetEntityCrudKitSettings} and {@code GetEntityCrudKits} handlers, with
 * {@code ProjectDetailsManagerImpl}'s settings methods. Slack settings are not ported (docs/01 §3.3).
 * <p>
 * Permissions are the legacy handlers': editing needs the setting's own action, and so does reading the project
 * settings, which include the webhooks. Reading the prefixes, the new entity settings and the languages needs only
 * {@code ViewProject} (docs/02 §3), because editors need them to create entities; the legacy handlers asked for the
 * edit actions, or for nothing at all.
 */
public class ProjectSettingsService {

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final WebhookRepository webhookRepository;

    private final MongoPrefixDeclarationsStore prefixDeclarationsStore;

    private final MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository;

    private final ProjectRegistry projectRegistry;

    public ProjectSettingsService(@Nonnull AccessManager accessManager,
                                  @Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                                  @Nonnull WebhookRepository webhookRepository,
                                  @Nonnull MongoPrefixDeclarationsStore prefixDeclarationsStore,
                                  @Nonnull MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository,
                                  @Nonnull ProjectRegistry projectRegistry) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.webhookRepository = checkNotNull(webhookRepository);
        this.prefixDeclarationsStore = checkNotNull(prefixDeclarationsStore);
        this.entityCrudKitSettingsRepository = checkNotNull(entityCrudKitSettingsRepository);
        this.projectRegistry = checkNotNull(projectRegistry);
    }

    /**
     * The project's name, description, languages and webhooks (legacy {@code GetProjectSettings}).
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     */
    @Nonnull
    public ProjectSettings getProjectSettings(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        var details = requireProject(caller, projectId, EDIT_PROJECT_SETTINGS);
        return toProjectSettings(details);
    }

    /**
     * Stores the settings of the project that they name and returns them as stored (legacy
     * {@code SetProjectSettings}); the webhooks are replaced.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     * @throws WpException               {@code INVALID_REQUEST} for a blank display name or a webhook that is not an
     *                                   http(s) URL
     */
    @Nonnull
    public ProjectSettings setProjectSettings(@Nonnull UserId caller, @Nonnull ProjectSettings settings) {
        var projectId = settings.getProjectId();
        var details = requireProject(caller, projectId, EDIT_PROJECT_SETTINGS);
        if (settings.getProjectDisplayName().isBlank()) {
            throw WpException.invalidRequest("A project needs a display name");
        }
        var webhooks = settings.getWebhookSettings().getWebhookSettings();
        webhooks.forEach(webhook -> requireWebhookUrl(webhook.getPayloadUrl()));
        projectDetailsRepository.save(details.withDisplayName(settings.getProjectDisplayName())
                                             .withDescription(settings.getProjectDescription())
                                             .withDefaultLanguage(settings.getDefaultLanguage())
                                             .withDefaultDisplayNameSettings(
                                                     settings.getDefaultDisplayNameSettings()));
        replaceWebhooks(projectId, webhooks);
        return toProjectSettings(requireProject(projectId));
    }

    /**
     * The project's webhooks, which are part of its settings.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     */
    @Nonnull
    public List<WebhookSetting> getWebhooks(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        requireProject(caller, projectId, EDIT_PROJECT_SETTINGS);
        return readWebhooks(projectId);
    }

    /**
     * Replaces the project's webhooks and returns them as stored.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     * @throws WpException               {@code INVALID_REQUEST} for a webhook that is not an http(s) URL
     */
    @Nonnull
    public List<WebhookSetting> setWebhooks(@Nonnull UserId caller,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull List<WebhookSetting> webhooks) {
        requireProject(caller, projectId, EDIT_PROJECT_SETTINGS);
        webhooks.forEach(webhook -> requireWebhookUrl(webhook.getPayloadUrl()));
        replaceWebhooks(projectId, webhooks);
        return readWebhooks(projectId);
    }

    /**
     * The project's default language and display name languages, and the languages its annotations use (the
     * language part of the legacy {@code GetProjectInfo}). The project is loaded if it is not.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public ProjectLanguages getProjectLanguages(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        var details = requireProject(caller, projectId, VIEW_PROJECT);
        return toProjectLanguages(details);
    }

    /**
     * Sets the project's default language and display name languages, the language part of the project settings.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     */
    @Nonnull
    public ProjectLanguages setProjectLanguages(@Nonnull UserId caller,
                                                @Nonnull ProjectId projectId,
                                                @Nonnull DictionaryLanguage defaultLanguage,
                                                @Nonnull DisplayNameSettings displayNameSettings) {
        var details = requireProject(caller, projectId, EDIT_PROJECT_SETTINGS);
        projectDetailsRepository.save(details.withDefaultLanguage(defaultLanguage)
                                             .withDefaultDisplayNameSettings(displayNameSettings));
        return toProjectLanguages(requireProject(projectId));
    }

    /**
     * The language tags that the project's annotations use, sorted (legacy {@code GetProjectLangTags}). The project
     * is loaded if it is not.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public List<LangTag> getLangTags(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        requireProject(caller, projectId, VIEW_PROJECT);
        return projectRegistry.get(projectId)
                              .languageManager()
                              .getActiveLanguages()
                              .stream()
                              .map(DictionaryLanguage::getLang)
                              .filter(lang -> !lang.isBlank())
                              .map(LangTag::get)
                              .distinct()
                              .sorted(Comparator.comparing(LangTag::getLanguageCode))
                              .toList();
    }

    /**
     * The project's prefix declarations (legacy {@code GetProjectPrefixDeclarations}).
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public PrefixDeclarations getPrefixDeclarations(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        requireProject(caller, projectId, VIEW_PROJECT);
        return prefixDeclarationsStore.find(projectId);
    }

    /**
     * Replaces the project's prefix declarations and returns them as stored (legacy
     * {@code SetProjectPrefixDeclarations}).
     *
     * @param prefixes prefix names, each ending with a colon, to prefixes
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project's prefixes
     * @throws WpException               {@code INVALID_REQUEST} for a prefix name without a colon at the end or a
     *                                   missing prefix
     */
    @Nonnull
    public PrefixDeclarations setPrefixDeclarations(@Nonnull UserId caller,
                                                    @Nonnull ProjectId projectId,
                                                    @Nonnull Map<String, String> prefixes) {
        requireProject(caller, projectId, EDIT_PROJECT_PREFIXES);
        PrefixDeclarations declarations;
        try {
            declarations = PrefixDeclarations.get(projectId, prefixes);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw WpException.invalidRequest(e.getMessage());
        }
        prefixDeclarationsStore.save(declarations);
        return prefixDeclarationsStore.find(projectId);
    }

    /**
     * How new entities get their IRIs (legacy {@code GetEntityCrudKitSettings}); a project without settings gets the
     * defaults, which are then stored, as the kernel's handler cache does. The project is loaded if it is not.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public EntityCrudKitSettings<?> getEntityCrudKitSettings(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        requireProject(caller, projectId, VIEW_PROJECT);
        return projectRegistry.get(projectId).crudKits().getHandler().getSettings();
    }

    /**
     * Stores how new entities get their IRIs (legacy {@code SetEntityCrudKitSettings}). With
     * {@link IRIPrefixUpdateStrategy#FIND_AND_REPLACE} the entities whose IRIs start with the old prefix are renamed
     * to the new one, in one revision by the caller. The old prefix is the stored one; the legacy handler took it
     * from the client.
     *
     * @return the settings as stored
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the new entity settings, or may not edit the
     *                                   ontology when entities are to be renamed
     */
    @Nonnull
    public EntityCrudKitSettings<?> setEntityCrudKitSettings(@Nonnull UserId caller,
                                                             @Nonnull ProjectId projectId,
                                                             @Nonnull EntityCrudKitSettings<?> settings,
                                                             @Nonnull IRIPrefixUpdateStrategy prefixUpdateStrategy) {
        requireProject(caller, projectId, EDIT_NEW_ENTITY_SETTINGS);
        var renameEntities = prefixUpdateStrategy == IRIPrefixUpdateStrategy.FIND_AND_REPLACE;
        if (renameEntities) {
            // The legacy ChangeManager checked this itself before it applied the renaming
            accessManager.require(caller, ProjectResource.of(projectId), EDIT_ONTOLOGY);
        }
        var context = projectRegistry.get(projectId);
        var fromPrefix = context.crudKits().getHandler().getSettings().getPrefixSettings().getIRIPrefix();
        entityCrudKitSettingsRepository.save(ProjectEntityCrudKitSettings.get(projectId, settings));
        var toPrefix = settings.getPrefixSettings().getIRIPrefix();
        if (renameEntities && !fromPrefix.equals(toPrefix)) {
            var indexes = context.indexes();
            var generators = new FindAndReplaceIRIPrefixChangeGeneratorFactory(
                    () -> indexes.get(ProjectSignatureIndex.class),
                    () -> new EntityRenamer(context.dataFactory(),
                                            indexes.get(ProjectOntologiesIndex.class),
                                            indexes.get(AxiomsByReferenceIndex.class)));
            context.changeManager().applyChanges(caller, generators.create(fromPrefix, toPrefix));
        }
        return context.crudKits().getHandler().getSettings();
    }

    /**
     * The kits that give new entities their IRI suffixes (legacy {@code GetEntityCrudKits}): UUIDs, OBO ids and
     * supplied names, the plugins that the kernel registers for every project.
     *
     * @throws PermissionDeniedException if the caller is the guest user
     */
    @Nonnull
    public List<EntityCrudKit<?>> getEntityCrudKits(@Nonnull UserId caller) {
        accessManager.requireSignedIn(caller);
        return List.of(new UuidSuffixKit(), new OBOIdSuffixKit(), new SuppliedNameSuffixKit());
    }

    private ProjectSettings toProjectSettings(ProjectDetails details) {
        return ProjectSettings.get(details.getProjectId(),
                                   details.getDisplayName(),
                                   details.getDescription(),
                                   details.getDefaultDictionaryLanguage(),
                                   details.getDefaultDisplayNameSettings(),
                                   WebhookSettings.get(readWebhooks(details.getProjectId())));
    }

    private ProjectLanguages toProjectLanguages(ProjectDetails details) {
        var languageUsage = projectRegistry.get(details.getProjectId()).activeLanguages().getLanguageUsage();
        return new ProjectLanguages(details.getDefaultDictionaryLanguage(),
                                    details.getDefaultDisplayNameSettings(),
                                    languageUsage);
    }

    private List<WebhookSetting> readWebhooks(ProjectId projectId) {
        return webhookRepository.getProjectWebhooks(projectId)
                                .stream()
                                .map(webhook -> WebhookSetting.get(webhook.getPayloadUrl(),
                                                                   ImmutableSet.copyOf(
                                                                           webhook.getSubscribedToEvents())))
                                .toList();
    }

    /**
     * As the legacy {@code setProjectSettings}: the stored webhooks are removed and the new ones inserted.
     */
    private void replaceWebhooks(ProjectId projectId, List<WebhookSetting> webhooks) {
        webhookRepository.clearProjectWebhooks(projectId);
        webhookRepository.addProjectWebhooks(webhooks.stream()
                                                     .map(webhook -> new ProjectWebhook(
                                                             projectId,
                                                             webhook.getPayloadUrl(),
                                                             new ArrayList<>(webhook.getEventTypes())))
                                                     .toList());
    }

    /**
     * Webhooks are called by the server, so they must be absolute http(s) URLs; the legacy server took any text.
     */
    private static void requireWebhookUrl(String payloadUrl) {
        try {
            var uri = new URI(payloadUrl);
            var scheme = uri.getScheme();
            if (uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return;
            }
        } catch (URISyntaxException e) {
            // reported below
        }
        throw WpException.invalidRequest("A webhook must be an http or https URL: " + payloadUrl);
    }

    private ProjectDetails requireProject(UserId caller, ProjectId projectId, BuiltInAction action) {
        var details = requireProject(projectId);
        accessManager.require(caller, ProjectResource.of(projectId), action);
        return details;
    }

    private ProjectDetails requireProject(ProjectId projectId) {
        return projectDetailsRepository.findOne(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
