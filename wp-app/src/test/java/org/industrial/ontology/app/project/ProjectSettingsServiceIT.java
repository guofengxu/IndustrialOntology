package org.industrial.ontology.app.project;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.IRIPrefixUpdateStrategy;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageUsage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.LangTag;
import org.industrial.ontology.domain.projectsettings.ProjectSettings;
import org.industrial.ontology.domain.projectsettings.WebhookSetting;
import org.industrial.ontology.domain.projectsettings.WebhookSettings;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.app.project.ProjectServiceIT.ALICE;
import static org.industrial.ontology.app.project.ProjectServiceIT.BOB;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link ProjectSettingsService} (07 5.1-12).
 */
class ProjectSettingsServiceIT {

    private static final String MENU = "http://www.industrial-ontology.org/test/menu#";

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectTestFixture fixture;

    private ProjectSettingsService settingsService;

    private ProjectRegistry projectRegistry;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory);
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        fixture = new ProjectTestFixture(context);
        settingsService = context.bean(ProjectSettingsService.class);
        projectRegistry = context.bean(ProjectRegistry.class);
    }

    @Test
    void theSettingsShouldBeStoredWithTheirWebhooks() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        var displayNames = DisplayNameSettings.get(ImmutableList.of(DictionaryLanguage.rdfsLabel("zh")),
                                                   ImmutableList.of(DictionaryLanguage.localName()));
        var webhook = WebhookSetting.get("https://hooks.example.org/pizza",
                                         Set.of(ProjectWebhookEventType.PROJECT_CHANGED));

        var stored = settingsService.setProjectSettings(ALICE, ProjectSettings.get(
                projectId, "Pizzeria", "All the pizzas", DictionaryLanguage.rdfsLabel("zh"), displayNames,
                WebhookSettings.get(List.of(webhook))));

        assertThat(stored).isEqualTo(settingsService.getProjectSettings(ALICE, projectId));
        assertThat(stored.getProjectDisplayName()).isEqualTo("Pizzeria");
        assertThat(stored.getProjectDescription()).isEqualTo("All the pizzas");
        assertThat(stored.getDefaultLanguage()).isEqualTo(DictionaryLanguage.rdfsLabel("zh"));
        assertThat(stored.getDefaultDisplayNameSettings()).isEqualTo(displayNames);
        assertThat(stored.getWebhookSettings().getWebhookSettings()).containsExactly(webhook);
        assertThat(settingsService.getWebhooks(ALICE, projectId)).containsExactly(webhook);
        assertThat(projectRegistry.get(projectId).languageManager().getLanguages())
                .containsExactly(DictionaryLanguage.rdfsLabel("zh"));

        var replaced = settingsService.setWebhooks(ALICE, projectId, List.of());
        assertThat(replaced).isEmpty();
        assertThat(settingsService.getProjectSettings(ALICE, projectId).getProjectDisplayName())
                .isEqualTo("Pizzeria");
    }

    @Test
    void theSettingsShouldNeedEditProjectSettings() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        fixture.grantProjectRoles(BOB, projectId, CAN_EDIT);
        var settings = settingsService.getProjectSettings(ALICE, projectId);

        assertThatThrownBy(() -> settingsService.getProjectSettings(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> settingsService.setProjectSettings(BOB, settings))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> settingsService.getWebhooks(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> settingsService.setProjectLanguages(BOB, projectId,
                                                                      DictionaryLanguage.rdfsLabel("fr"),
                                                                      DisplayNameSettings.empty()))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void blankNamesAndWebhooksThatAreNotHttpUrlsShouldBeRefused() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        var settings = settingsService.getProjectSettings(ALICE, projectId);

        assertThatThrownBy(() -> settingsService.setProjectSettings(ALICE, ProjectSettings.get(
                projectId, " ", "", settings.getDefaultLanguage(), settings.getDefaultDisplayNameSettings(),
                settings.getWebhookSettings())))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        for (var url : List.of("file:///etc/passwd", "hooks.example.org/pizza", "https:///no-host", "http://a b")) {
            assertThatThrownBy(() -> settingsService.setWebhooks(ALICE, projectId,
                                                                 List.of(WebhookSetting.get(url, Set.of()))))
                    .as(url)
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo("INVALID_REQUEST");
        }
        assertThat(settingsService.getWebhooks(ALICE, projectId)).isEmpty();
    }

    @Test
    void viewersShouldSeeTheLanguagesAndTheLanguageTagsInUse() {
        var projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);

        var languages = settingsService.getProjectLanguages(BOB, projectId);

        assertThat(languages.defaultLanguage()).isEqualTo(DictionaryLanguage.rdfsLabel("en"));
        assertThat(languages.languageUsage()).extracting(DictionaryLanguageUsage::dictionaryLanguage)
                                             .contains(DictionaryLanguage.rdfsLabel("en"),
                                                       DictionaryLanguage.rdfsLabel("zh"));
        assertThat(settingsService.getLangTags(BOB, projectId)).containsExactly(LangTag.get("en"), LangTag.get("zh"));

        var chineseFirst = DisplayNameSettings.get(ImmutableList.of(DictionaryLanguage.rdfsLabel("zh")),
                                                   ImmutableList.of());
        var changed = settingsService.setProjectLanguages(ALICE, projectId, DictionaryLanguage.rdfsLabel("zh"),
                                                          chineseFirst);
        assertThat(changed.displayNameSettings()).isEqualTo(chineseFirst);
        assertThat(changed.defaultLanguage()).isEqualTo(DictionaryLanguage.rdfsLabel("zh"));
        assertThat(projectRegistry.get(projectId).languageManager().getLanguages())
                .containsExactly(DictionaryLanguage.rdfsLabel("zh"));
    }

    @Test
    void prefixesShouldBeReadByViewersAndWrittenByManagers() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        fixture.grantProjectRoles(BOB, projectId, CAN_EDIT);

        var stored = settingsService.setPrefixDeclarations(ALICE, projectId, Map.of("pizza:", PizzaOntology.NAMESPACE));

        assertThat(stored.getPrefixes()).containsExactly(Map.entry("pizza:", PizzaOntology.NAMESPACE));
        assertThat(settingsService.getPrefixDeclarations(BOB, projectId).getPrefixes())
                .isEqualTo(stored.getPrefixes());
        assertThatThrownBy(() -> settingsService.setPrefixDeclarations(BOB, projectId, Map.of()))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> settingsService.setPrefixDeclarations(ALICE, projectId,
                                                                       Map.of("pizza", PizzaOntology.NAMESPACE)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> settingsService.getPrefixDeclarations(UserId.getUserId("carol"), projectId))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void newEntitySettingsShouldStartWithTheDefaultsAndBeStored() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        fixture.grantProjectRoles(BOB, projectId, CAN_EDIT);

        var defaults = settingsService.getEntityCrudKitSettings(BOB, projectId);
        assertThat(defaults.getPrefixSettings()).isEqualTo(EntityCrudKitPrefixSettings.get());
        assertThat(defaults.getSuffixSettings()).isEqualTo(UuidSuffixSettings.get());

        var suppliedNames = EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(MENU, ImmutableList.of()),
                                                      SuppliedNameSuffixSettings.get());
        assertThatThrownBy(() -> settingsService.setEntityCrudKitSettings(BOB, projectId, suppliedNames,
                                                                          IRIPrefixUpdateStrategy.LEAVE_INTACT))
                .isInstanceOf(PermissionDeniedException.class);
        assertThat(settingsService.setEntityCrudKitSettings(ALICE, projectId, suppliedNames,
                                                            IRIPrefixUpdateStrategy.LEAVE_INTACT))
                .isEqualTo(suppliedNames);
        assertThat(settingsService.getEntityCrudKitSettings(BOB, projectId)).isEqualTo(suppliedNames);
        assertThat(projectRegistry.get(projectId).revisionManager().getRevisions()).isEmpty();
    }

    @Test
    void findAndReplaceShouldRenameTheEntitiesWithTheOldPrefix() {
        var projectId = fixture.createPizzaProject(ALICE, sources);
        settingsService.setEntityCrudKitSettings(ALICE, projectId, crudSettings(PizzaOntology.NAMESPACE),
                                                 IRIPrefixUpdateStrategy.LEAVE_INTACT);

        settingsService.setEntityCrudKitSettings(ALICE, projectId, crudSettings(MENU),
                                                 IRIPrefixUpdateStrategy.FIND_AND_REPLACE);

        var project = projectRegistry.get(projectId);
        var signature = project.indexes().get(EntitiesInProjectSignatureByIriIndex.class);
        assertThat(signature.getEntitiesInSignature(IRI.create(MENU + "Margherita"))).isNotEmpty();
        assertThat(signature.getEntitiesInSignature(PizzaOntology.iri("Margherita"))).isEmpty();
        assertThat(project.revisionManager().getRevisions()).hasSize(2)
                                                            .last()
                                                            .satisfies(revision -> assertThat(revision.getUserId())
                                                                    .isEqualTo(ALICE));
        assertThat(settingsService.getEntityCrudKitSettings(ALICE, projectId).getPrefixSettings().getIRIPrefix())
                .isEqualTo(MENU);
    }

    /**
     * No built-in role has {@code EditNewEntitySettings} without {@code EditOntology}, so the assignment is written
     * with its closure by hand.
     */
    @Test
    void findAndReplaceShouldAlsoNeedEditOntology() {
        var projectId = fixture.createPizzaProject(ALICE, sources);
        context.bean(RoleAssignmentRepository.class)
               .setAssignedRoles(BOB.getUserName(), projectId.getId(), List.of(), List.of(),
                                 List.of("ViewProject", "EditNewEntitySettings"));

        assertThatThrownBy(() -> settingsService.setEntityCrudKitSettings(BOB, projectId, crudSettings(MENU),
                                                                          IRIPrefixUpdateStrategy.FIND_AND_REPLACE))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("EditOntology");
        assertThat(settingsService.setEntityCrudKitSettings(BOB, projectId, crudSettings(MENU),
                                                            IRIPrefixUpdateStrategy.LEAVE_INTACT))
                .isEqualTo(crudSettings(MENU));
        assertThat(projectRegistry.get(projectId).revisionManager().getRevisions()).hasSize(1);
    }

    @Test
    void theCrudKitsShouldBeTheKernelsThree() {
        assertThat(settingsService.getEntityCrudKits(BOB))
                .extracting(kit -> kit.getKitId().getLexicalForm())
                .containsExactly("UUID", "OBO", "SuppliedNameSuffix");
        assertThatThrownBy(() -> settingsService.getEntityCrudKits(UserId.getGuest()))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void unknownProjectsShouldBeNotFound() {
        var unknown = ProjectId.get("00000000-0000-0000-0000-000000000001");

        assertThatThrownBy(() -> settingsService.getProjectLanguages(ALICE, unknown))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThatThrownBy(() -> settingsService.getEntityCrudKitSettings(ALICE, unknown))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThat(projectRegistry.getIfLoaded(unknown)).isEmpty();
    }

    private static EntityCrudKitSettings<?> crudSettings(String prefix) {
        return EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(prefix, ImmutableList.of()),
                                         UuidSuffixSettings.get());
    }

    /**
     * The legacy {@code GetProjectSettingsActionHandler_TestCase} and {@code SetProjectSettingsActionHandler_TestCase}.
     */
    @Nested
    class Legacy {

        @Test
        void shouldReturnSettings() {
            var projectId = fixture.createProject(ALICE, "Pizza");

            var settings = settingsService.getProjectSettings(ALICE, projectId);

            assertThat(settings.getProjectId()).isEqualTo(projectId);
            assertThat(settings.getProjectDisplayName()).isEqualTo("Pizza");
        }

        @Test
        void shouldSetProjectSettings() {
            var projectId = fixture.createProject(ALICE, "Pizza");
            var settings = settingsService.getProjectSettings(ALICE, projectId);

            settingsService.setProjectSettings(ALICE, ProjectSettings.get(projectId, "Pizzas", "Description",
                                                                           settings.getDefaultLanguage(),
                                                                           settings.getDefaultDisplayNameSettings(),
                                                                           settings.getWebhookSettings()));

            assertThat(settingsService.getProjectSettings(ALICE, projectId).getProjectDisplayName())
                    .isEqualTo("Pizzas");
        }
    }
}
