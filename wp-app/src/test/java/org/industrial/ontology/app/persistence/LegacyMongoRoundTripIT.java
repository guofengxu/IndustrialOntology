package org.industrial.ontology.app.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesDocument;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.apikey.persistence.UserApiKeyRepository;
import org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument.ApiKeyRecord;
import org.industrial.ontology.app.form.persistence.MongoEntityFormRepository;
import org.industrial.ontology.app.form.persistence.MongoEntityFormSelectorRepository;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorRepository;
import org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument;
import org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument;
import org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessDocument;
import org.industrial.ontology.app.project.persistence.ProjectAccessRepository;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.tag.persistence.EntityTagsDocument;
import org.industrial.ontology.app.tag.persistence.EntityTagsRepository;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.app.user.persistence.UserActivityDocument;
import org.industrial.ontology.app.user.persistence.UserActivityRepository;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.app.watch.persistence.WatchDocument;
import org.industrial.ontology.app.watch.persistence.WatchRepository;
import org.industrial.ontology.app.webhook.persistence.WebhookRepository;
import org.industrial.ontology.domain.app.ApplicationLocation;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.form.EntityFormSelector;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.CommentId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import org.industrial.ontology.domain.match.EntityIsDeprecatedCriteria;
import org.industrial.ontology.domain.match.EntityTypeIsOneOfCriteria;
import org.industrial.ontology.domain.match.HierarchyFilterType;
import org.industrial.ontology.domain.match.MultiMatchType;
import org.industrial.ontology.domain.match.SubClassOfCriteria;
import org.industrial.ontology.domain.perspective.PerspectiveDescriptor;
import org.industrial.ontology.domain.perspective.PerspectiveId;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.industrial.ontology.domain.watches.WatchType;
import org.industrial.ontology.domain.webhook.ProjectWebhook;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance test of S4 (docs/07): every repository reads the legacy documents in {@code legacy-mongo/} into the
 * right objects, and writing them back through the repository leaves the documents exactly as the legacy code wrote
 * them, field order and BSON types included, except that the {@code className} which old Morphia versions stored is
 * gone (5.3-2). The samples are imported with such a {@code className}: at the top level of every document, and in
 * the objects that Morphia embedded.
 * <p>
 * Two collections cannot come back byte for byte, by design of the legacy code: {@code ProjectAccess} is only ever
 * updated with {@code $inc}, and webhooks are replaced by deleting and inserting them, which gives them new ids.
 */
class LegacyMongoRoundTripIT {

    private static final ProjectId PIZZA_PROJECT = ProjectId.get("2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81");

    private static final ProjectId WINE_PROJECT = ProjectId.get("7c1f0e9d-8b2a-4d3c-a1e5-0f9e8d7c6b52");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final String PIZZA = "http://www.co-ode.org/ontologies/pizza/pizza.owl#";

    private static final long CREATED = 1_600_000_000_000L;

    private static final long MODIFIED = 1_650_000_000_123L;

    private static final PerspectiveId CLASSES = PerspectiveId.get("69df8fa8-4f84-499e-9341-28eb5085c40b");

    private static final PerspectiveId COMMENTS = PerspectiveId.get("0e1a2b3c-4d5e-4f60-8172-839405a6b7c8");

    private static final String LEGACY_PACKAGE = "edu.stanford.bmir.protege.web.";

    private static final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private static final ObjectMapper objectMapper = new ObjectMapperProvider().get();

    private static MongoPersistenceTestContext context;

    @BeforeAll
    static void startContext() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void closeContext() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
    }

    @Test
    void users() {
        importSample("Users", "server.user.UserRecord");
        var repository = context.bean(UserRecordRepository.class);

        var alice = repository.findOne(ALICE).orElseThrow();
        var bob = repository.findOne(BOB).orElseThrow();
        assertThat(alice).isEqualTo(new UserRecordDocument("alice", "Alice Liddell", "alice@example.org",
                                                           "https://example.org/avatars/alice.png",
                                                           "000102030405060708090a0b0c0d0e0f",
                                                           "deadbeef0123456789abcdef10325476", null));
        assertThat(bob.getAvatarUrl()).isEmpty();
        assertThat(bob.emailAddress()).isEmpty();

        repository.save(alice);
        repository.save(bob);
        assertWrittenBackUnchanged("Users");
    }

    @Test
    void userActivity() {
        importSample("UserActivity", "server.user.UserActivityRecord");
        var repository = context.bean(UserActivityRepository.class);

        var alice = repository.getUserActivityRecord(ALICE).orElseThrow();
        var bob = repository.getUserActivityRecord(BOB).orElseThrow();
        assertThat(alice).isEqualTo(new UserActivityDocument(
                "alice", Instant.ofEpochMilli(MODIFIED), Instant.ofEpochMilli(CREATED),
                List.of(UserActivityDocument.RecentProject.of(PIZZA_PROJECT, MODIFIED),
                        UserActivityDocument.RecentProject.of(WINE_PROJECT, CREATED))));
        assertThat(bob).isEqualTo(UserActivityDocument.empty(BOB));

        repository.save(alice);
        repository.save(bob);
        assertWrittenBackUnchanged("UserActivity");
    }

    @Test
    void projectDetails() {
        importSample("ProjectDetails", "shared.project.ProjectDetails");
        var repository = context.bean(MongoProjectDetailsRepository.class);

        var pizza = repository.findOne(PIZZA_PROJECT).orElseThrow();
        var wine = repository.findOne(WINE_PROJECT).orElseThrow();
        var english = AnnotationAssertionDictionaryLanguage.get(OWLRDFVocabulary.RDFS_LABEL.getIRI(), "en");
        var chinese = AnnotationAssertionDictionaryLanguage.get(OWLRDFVocabulary.RDFS_LABEL.getIRI(), "zh");
        assertThat(pizza).isEqualTo(ProjectDetails.get(PIZZA_PROJECT, "Pizza", "The pizza ontology", ALICE, false,
                                                       english,
                                                       DisplayNameSettings.get(ImmutableList.of(english, chinese),
                                                                               ImmutableList.of()),
                                                       CREATED, ALICE, MODIFIED, BOB));
        assertThat(wine).isEqualTo(ProjectDetails.get(WINE_PROJECT, "Wine", "", BOB, true, english,
                                                      DisplayNameSettings.empty(), CREATED, BOB, CREATED, BOB));
        assertThat(repository.getDisplayNameLanguages(PIZZA_PROJECT)).containsExactly(english, chinese);

        repository.save(pizza);
        repository.save(wine);
        assertWrittenBackUnchanged("ProjectDetails");
    }

    @Test
    void projectAccess() {
        importSample("ProjectAccess", null);
        var repository = context.bean(ProjectAccessRepository.class);

        assertThat(repository.findAccess(PIZZA_PROJECT, ALICE)).contains(new ProjectAccessDocument(
                new ObjectId("5f0c0c0c0c0c0c0c0c000001"), PIZZA_PROJECT.getId(), "alice",
                Instant.ofEpochMilli(MODIFIED), 3));

        // The legacy manager only logs accesses; logging one more at the same time only changes the count.
        repository.logProjectAccess(PIZZA_PROJECT, ALICE, MODIFIED);
        var expected = LegacyMongoSamples.documents("ProjectAccess").get(0).append("count", 4);
        assertThat(LegacyMongoSamples.stored(context.database(), "ProjectAccess"))
                .containsExactly(LegacyMongoSamples.canonical(expected));
    }

    @Test
    void roleAssignments() {
        importSample("RoleAssignments", "server.access.RoleAssignment");
        var repository = context.bean(RoleAssignmentRepository.class);

        var assignments = repository.findAll();
        var samples = LegacyMongoSamples.documents("RoleAssignments");
        assertThat(assignments).hasSize(4);
        for (int i = 0; i < samples.size(); i++) {
            var sample = samples.get(i);
            var assignment = assignments.get(i);
            assertThat(assignment.id()).isEqualTo(sample.getObjectId("_id"));
            assertThat(assignment.userName()).isEqualTo(sample.getString("userName"));
            assertThat(assignment.projectId()).isEqualTo(sample.getString("projectId"));
            assertThat(assignment.assignedRoles()).isEqualTo(sample.getList("assignedRoles", String.class));
            assertThat(assignment.roleClosure()).isEqualTo(sample.getList("roleClosure", String.class));
            assertThat(assignment.actionClosure()).isEqualTo(sample.getList("actionClosure", String.class));
        }
        assertThat(assignments.get(2).projectId()).isNull();
        assertThat(assignments.get(3).userName()).isNull();

        assignments.forEach(repository::save);
        assertWrittenBackUnchanged("RoleAssignments");
    }

    @Test
    void prefixDeclarations() {
        importSample("PrefixDeclarations", "shared.project.PrefixDeclarations");
        var store = context.bean(MongoPrefixDeclarationsStore.class);

        var prefixes = store.find(PIZZA_PROJECT);
        assertThat(prefixes).isEqualTo(PrefixDeclarations.get(PIZZA_PROJECT, ImmutableMap.of(
                "pizza:", PIZZA, "owl:", "http://www.w3.org/2002/07/owl#", ":", PIZZA)));
        assertThat(prefixes.getPrefixes().keySet()).containsExactly("pizza:", "owl:", ":");

        store.save(prefixes);
        assertWrittenBackUnchanged("PrefixDeclarations");
    }

    @Test
    void entityCrudKitSettings() {
        importSample("EntityCrudKitSettings", "server.crud.persistence.ProjectEntityCrudKitSettings");
        var repository = context.bean(MongoEntityCrudKitSettingsRepository.class);

        var settings = repository.findOne(PIZZA_PROJECT).orElseThrow();
        assertThat(settings).isEqualTo(ProjectEntityCrudKitSettings.get(
                PIZZA_PROJECT,
                EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(PIZZA, ImmutableList.of()),
                                          UuidSuffixSettings.get())));

        repository.save(settings);
        assertWrittenBackUnchanged("EntityCrudKitSettings");
    }

    @Test
    void perspectiveDescriptors() {
        importSample("PerspectiveDescriptors", "server.perspective.PerspectiveDescriptorsRecord");
        var repository = context.bean(PerspectiveDescriptorRepository.class);

        var classes = PerspectiveDescriptor.get(CLASSES, LanguageMap.of("en", "Classes"), true);
        var comments = PerspectiveDescriptor.get(COMMENTS, LanguageMap.of("en", "Comments"), false);
        var system = repository.findDescriptors().orElseThrow();
        var alice = repository.findDescriptors(PIZZA_PROJECT, ALICE).orElseThrow();
        assertThat(system).isEqualTo(PerspectiveDescriptorsDocument.get(ImmutableList.of(classes, comments)));
        assertThat(alice).isEqualTo(PerspectiveDescriptorsDocument.get(PIZZA_PROJECT, ALICE,
                                                                       ImmutableList.of(classes)));
        assertThat(repository.findDescriptors(PIZZA_PROJECT)).isEmpty();
        assertThat(repository.findProjectAndSystemDescriptors(PIZZA_PROJECT)).containsExactly(system);

        repository.saveDescriptors(system);
        repository.saveDescriptors(alice);
        assertWrittenBackUnchanged("PerspectiveDescriptors");
    }

    @Test
    void perspectiveLayouts() throws IOException {
        importSample("PerspectiveLayouts", "server.perspective.PerspectiveLayoutRecord");
        var repository = context.bean(PerspectiveLayoutRepository.class);

        var alice = repository.findLayout(PIZZA_PROJECT, ALICE, CLASSES).orElseThrow();
        var builtIn = repository.findLayout(COMMENTS).orElseThrow();
        var layout = objectMapper.readTree("""
                {"@type": "ParentNode", "direction": "row", "children": [
                  {"weight": 0.3, "node": {"@type": "LeafNode", "id": "N-0",
                    "properties": {"portlet": {"@type": "String", "value": "portlets.ClassHierarchy"}}}},
                  {"weight": 0.7, "node": {"@type": "LeafNode", "id": "N-1",
                    "properties": {"portlet": {"@type": "String", "value": "portlets.ClassEditor"}}}}]}
                """);
        assertThat(alice).isEqualTo(new PerspectiveLayoutDocument(PIZZA_PROJECT, ALICE, CLASSES, layout));
        assertThat(builtIn).isEqualTo(new PerspectiveLayoutDocument(null, null, COMMENTS, null));
        assertThat(builtIn.toPerspectiveLayout().getLayout()).isEmpty();

        repository.saveLayouts(List.of(alice, builtIn));
        assertWrittenBackUnchanged("PerspectiveLayouts");
    }

    @Test
    void forms() throws IOException {
        importSample("Forms", "server.form.FormDescriptorRecord");
        var repository = context.bean(MongoEntityFormRepository.class);

        FormDescriptor expected;
        try (var in = getClass().getResourceAsStream("/forms/pizza-form.json")) {
            expected = objectMapper.readValue(in, FormDescriptor.class);
        }
        var forms = repository.findFormDescriptors(PIZZA_PROJECT).toList();
        assertThat(forms).containsExactly(expected);
        assertThat(repository.findFormDescriptor(PIZZA_PROJECT, expected.getFormId())).contains(expected);

        repository.saveFormDescriptor(PIZZA_PROJECT, forms.get(0));
        assertWrittenBackUnchanged("Forms");
    }

    @Test
    void formSelectors() {
        importSample("FormSelectors", "shared.form.EntityFormSelector");
        var repository = context.bean(MongoEntityFormSelectorRepository.class);

        var selectors = repository.findFormSelectors(PIZZA_PROJECT).toList();
        var criteria = CompositeRootCriteria.get(ImmutableList.of(
                SubClassOfCriteria.get(cls("Pizza"), HierarchyFilterType.ALL),
                EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.NAMED_INDIVIDUAL))), MultiMatchType.ALL);
        assertThat(selectors).containsExactly(EntityFormSelector.get(
                PIZZA_PROJECT, criteria, FormId.get("8b3c4d5e-6f70-4182-93a4-b5c6d7e8f901")));

        repository.save(selectors.get(0));
        assertWrittenBackUnchanged("FormSelectors");
    }

    @Test
    void tags() {
        importSample("Tags", "shared.tag.Tag");
        var repository = context.bean(TagRepository.class);

        var tags = repository.findTags(PIZZA_PROJECT);
        assertThat(tags).containsExactly(
                Tag.get(TagId.getId("5d6e7f80-91a2-4b3c-8d4e-5f60718293a4"), PIZZA_PROJECT, "Deprecated",
                        "Entities that should no longer be used", Color.getHex("#ffffff"), Color.getHex("#c0392b"),
                        ImmutableList.of(EntityIsDeprecatedCriteria.get())),
                Tag.get(TagId.getId("6e7f8091-a2b3-4c4d-9e5f-60718293a4b5"), PIZZA_PROJECT, "Needs review", "",
                        Color.getHex("#000000"), Color.getHex("#f1c40f"), ImmutableList.of()));

        repository.saveTags(tags);
        assertWrittenBackUnchanged("Tags");
    }

    @Test
    void entityTags() {
        importSample("EntityTags", "server.tag.EntityTags");
        var repository = context.bean(EntityTagsRepository.class);

        var margherita = repository.findByEntity(PIZZA_PROJECT, cls("Margherita")).orElseThrow();
        assertThat(margherita).isEqualTo(new EntityTagsDocument(
                new ObjectId("5f0c0c0c0c0c0c0c0c00000c"), PIZZA_PROJECT.getId(), cls("Margherita"),
                List.of("5d6e7f80-91a2-4b3c-8d4e-5f60718293a4", "6e7f8091-a2b3-4c4d-9e5f-60718293a4b5")));

        repository.save(margherita);
        assertWrittenBackUnchanged("EntityTags");
    }

    @Test
    void watches() {
        importSample("Watches", "server.watches.WatchRecord");
        var repository = context.bean(WatchRepository.class);

        var alice = repository.findWatches(PIZZA_PROJECT, ALICE);
        var bob = repository.findWatches(PIZZA_PROJECT, BOB);
        var hasTopping = dataFactory.getOWLObjectProperty(IRI.create(PIZZA + "hasTopping"));
        assertThat(alice).containsExactly(new WatchDocument(new ObjectId("5f0c0c0c0c0c0c0c0c00000d"),
                                                            PIZZA_PROJECT.getId(), "alice", cls("Pizza"),
                                                            WatchType.BRANCH));
        assertThat(bob).containsExactly(new WatchDocument(new ObjectId("5f0c0c0c0c0c0c0c0c00000e"),
                                                          PIZZA_PROJECT.getId(), "bob", hasTopping,
                                                          WatchType.ENTITY));

        repository.saveWatch(alice.get(0));
        repository.saveWatch(bob.get(0));
        assertWrittenBackUnchanged("Watches");
    }

    @Test
    void entityDiscussionThreads() {
        importSample("EntityDiscussionThreads", "shared.issues.EntityDiscussionThread");
        var repository = context.bean(DiscussionThreadRepository.class);

        var threads = repository.getThreadsInProject(PIZZA_PROJECT);
        assertThat(threads).containsExactly(
                new EntityDiscussionThread(
                        new ThreadId("b1c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e"), PIZZA_PROJECT, cls("AmericanHot"),
                        Status.OPEN,
                        ImmutableList.of(new Comment(CommentId.fromString("c0a80101-0000-4000-8000-000000000001"),
                                                     ALICE, CREATED, Optional.of(MODIFIED),
                                                     "Is @bob sure about this?",
                                                     "<p>Is <a href=\"#bob\">@bob</a> sure about this?</p>"),
                                         new Comment(CommentId.fromString("c0a80101-0000-4000-8000-000000000002"),
                                                     BOB, MODIFIED, Optional.empty(), "Yes.", "<p>Yes.</p>"))),
                new EntityDiscussionThread(
                        new ThreadId("c2d3e4f5-a6b7-4c8d-9e0f-1a2b3c4d5e6f"), PIZZA_PROJECT,
                        dataFactory.getOWLNamedIndividual(IRI.create(PIZZA + "Italy")), Status.CLOSED,
                        ImmutableList.of()));
        assertThat(repository.getOpenCommentsCount(PIZZA_PROJECT, cls("AmericanHot"))).isEqualTo(2);

        threads.forEach(repository::saveThread);
        assertWrittenBackUnchanged("EntityDiscussionThreads");
    }

    @Test
    void projectWebhooks() {
        importSample("ProjectWebhook", "shared.webhook.ProjectWebhook");
        var repository = context.bean(WebhookRepository.class);

        var webhooks = repository.getProjectWebhooks(PIZZA_PROJECT);
        assertThat(webhooks).containsExactly(new ProjectWebhook(PIZZA_PROJECT, "https://hooks.example.org/pizza",
                                                                List.of(ProjectWebhookEventType.PROJECT_CHANGED)));

        // Webhooks are replaced as a whole, so they get new ids; everything else is as the legacy code wrote it.
        repository.clearProjectWebhooks(PIZZA_PROJECT);
        repository.addProjectWebhooks(webhooks);
        var stored = context.database().getCollection("ProjectWebhook").find().into(new ArrayList<>());
        var sample = LegacyMongoSamples.documents("ProjectWebhook");
        assertThat(stored).hasSize(1);
        assertThat(stored.get(0).get("_id")).isInstanceOf(ObjectId.class);
        stored.get(0).put("_id", sample.get(0).get("_id"));
        assertThat(LegacyMongoSamples.canonical(stored.get(0))).isEqualTo(LegacyMongoSamples.lines("ProjectWebhook")
                                                                                            .get(0));
    }

    @Test
    void userApiKeys() {
        importSample("UserApiKeys", "server.api.UserApiKeys");
        var repository = context.bean(UserApiKeyRepository.class);

        var keys = repository.getApiKeys(ALICE);
        assertThat(keys).containsExactly(
                new ApiKeyRecord("d3e4f5a6-b7c8-4d9e-8f01-2a3b4c5d6e7f",
                                 "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                                 Instant.ofEpochMilli(CREATED), "CI pipeline"),
                new ApiKeyRecord("e4f5a6b7-c8d9-4e0f-9a12-3b4c5d6e7f80",
                                 "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752",
                                 Instant.ofEpochMilli(MODIFIED), "Notebook"));
        assertThat(repository.getUserIdForApiKey(keys.get(1).apiKey())).contains(ALICE);

        repository.setApiKeys(ALICE, keys);
        assertWrittenBackUnchanged("UserApiKeys");
    }

    @Test
    void applicationPreferences() {
        importSample("ApplicationPreferences", "server.app.ApplicationPreferences");
        var repository = context.bean(ApplicationPreferencesRepository.class);

        var preferences = repository.getApplicationPreferences();
        assertThat(preferences).isEqualTo(ApplicationPreferencesDocument.of(
                "IndustrialOntology", "admin@example.org",
                new ApplicationLocation("https", "ontology.example.org", "/webprotege", 443), Long.MAX_VALUE));

        repository.setApplicationPreferences(preferences);
        assertWrittenBackUnchanged("ApplicationPreferences");
    }

    @Test
    void entitySearchFilters() {
        importSample("EntitySearchFilters", "shared.search.EntitySearchFilter");
        var repository = context.bean(EntitySearchFilterRepository.class);

        var filters = repository.getSearchFilters(PIZZA_PROJECT);
        assertThat(filters).containsExactly(EntitySearchFilter.get(
                EntitySearchFilterId.get("f5a6b7c8-d9e0-4f1a-8b23-4c5d6e7f8091"), PIZZA_PROJECT,
                LanguageMap.of("en", "Deprecated entities"), EntityIsDeprecatedCriteria.get()));

        repository.saveSearchFilters(filters);
        assertWrittenBackUnchanged("EntitySearchFilters");
    }

    /**
     * Inserts the collection's sample with a {@code className} as old Morphia versions stored it: at the top level,
     * and, for the collections Morphia wrote, in every embedded object except the entity, which went through a
     * converter.
     *
     * @param legacyClass the stored class, relative to {@code edu.stanford.bmir.protege.web.}; {@code null} for a
     *                    collection that Morphia never wrote
     */
    private static void importSample(String collection, String legacyClass) {
        var documents = LegacyMongoSamples.documents(collection);
        if (legacyClass != null) {
            var morphia = Stream.of(LegacyCollection.values())
                                .filter(legacyCollection -> legacyCollection.collectionName().equals(collection))
                                .findFirst()
                                .orElseThrow()
                                .storage() == LegacyCollection.Storage.MORPHIA;
            documents.forEach(document -> {
                document.put(MongoMigration.CLASS_NAME, LEGACY_PACKAGE + legacyClass);
                if (morphia) {
                    document.forEach((field, value) -> addEmbeddedClassNames(field, value));
                }
            });
        }
        context.database().getCollection(collection).insertMany(documents);
    }

    private static void addEmbeddedClassNames(String field, Object value) {
        if (field.equals("entity")) {
            return;
        }
        if (value instanceof Document embedded) {
            embedded.put(MongoMigration.CLASS_NAME, LEGACY_PACKAGE + "embedded." + field);
        } else if (value instanceof List<?> list) {
            list.forEach(element -> addEmbeddedClassNames(field, element));
        }
    }

    private static void assertWrittenBackUnchanged(String collection) {
        assertThat(LegacyMongoSamples.stored(context.database(), collection))
                .containsExactlyElementsOf(LegacyMongoSamples.lines(collection));
    }

    private static OWLClass cls(String name) {
        return dataFactory.getOWLClass(IRI.create(PIZZA + name));
    }
}
