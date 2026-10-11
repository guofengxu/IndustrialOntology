package org.industrial.ontology.compat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import edu.stanford.bmir.protege.web.server.access.RoleAssignment;
import edu.stanford.bmir.protege.web.server.access.RoleOracleImpl;
import edu.stanford.bmir.protege.web.server.api.ApiKeyRecord;
import edu.stanford.bmir.protege.web.server.api.HashedApiKey;
import edu.stanford.bmir.protege.web.server.api.UserApiKeyStoreImpl;
import edu.stanford.bmir.protege.web.server.api.UserApiKeys;
import edu.stanford.bmir.protege.web.server.app.ApplicationPreferences;
import edu.stanford.bmir.protege.web.server.collection.CollectionIdConverter;
import edu.stanford.bmir.protege.web.server.color.ColorConverter;
import edu.stanford.bmir.protege.web.server.crud.persistence.ProjectEntityCrudKitSettings;
import edu.stanford.bmir.protege.web.server.form.EntityFormRepositoryImpl;
import edu.stanford.bmir.protege.web.server.form.EntityFormSelectorRepositoryImpl;
import edu.stanford.bmir.protege.web.server.form.FormDescriptorRecord;
import edu.stanford.bmir.protege.web.server.form.FormIdConverter;
import edu.stanford.bmir.protege.web.server.jackson.ObjectMapperProvider;
import edu.stanford.bmir.protege.web.server.persistence.CommentIdConverter;
import edu.stanford.bmir.protege.web.server.persistence.MorphiaProvider;
import edu.stanford.bmir.protege.web.server.persistence.OWLEntityConverter;
import edu.stanford.bmir.protege.web.server.persistence.ProjectIdConverter;
import edu.stanford.bmir.protege.web.server.persistence.ThreadIdConverter;
import edu.stanford.bmir.protege.web.server.persistence.UserIdConverter;
import edu.stanford.bmir.protege.web.server.perspective.PerspectiveDescriptorRepositoryImpl;
import edu.stanford.bmir.protege.web.server.perspective.PerspectiveDescriptorsRecord;
import edu.stanford.bmir.protege.web.server.perspective.PerspectiveLayoutRecord;
import edu.stanford.bmir.protege.web.server.perspective.PerspectiveLayoutRepositoryImpl;
import edu.stanford.bmir.protege.web.server.project.ProjectAccessManagerImpl;
import edu.stanford.bmir.protege.web.server.project.ProjectDetailsRepository;
import edu.stanford.bmir.protege.web.server.project.RecentProjectRecord;
import edu.stanford.bmir.protege.web.server.search.EntitySearchFilterRepositoryImpl;
import edu.stanford.bmir.protege.web.server.tag.EntityTags;
import edu.stanford.bmir.protege.web.server.tag.EntityTagsRepositoryImpl;
import edu.stanford.bmir.protege.web.server.tag.TagIdConverter;
import edu.stanford.bmir.protege.web.server.tag.TagRepositoryImpl;
import edu.stanford.bmir.protege.web.server.user.UserActivityManager;
import edu.stanford.bmir.protege.web.server.user.UserActivityRecord;
import edu.stanford.bmir.protege.web.server.user.UserRecord;
import edu.stanford.bmir.protege.web.server.user.UserRecordConverter;
import edu.stanford.bmir.protege.web.server.watches.WatchRecord;
import edu.stanford.bmir.protege.web.server.watches.WatchRecordRepositoryImpl;
import edu.stanford.bmir.protege.web.shared.access.BuiltInRole;
import edu.stanford.bmir.protege.web.shared.access.RoleId;
import edu.stanford.bmir.protege.web.shared.api.ApiKeyId;
import edu.stanford.bmir.protege.web.shared.app.ApplicationLocation;
import edu.stanford.bmir.protege.web.shared.auth.Salt;
import edu.stanford.bmir.protege.web.shared.auth.SaltedPasswordDigest;
import edu.stanford.bmir.protege.web.shared.color.Color;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitPrefixSettings;
import edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitSettings;
import edu.stanford.bmir.protege.web.shared.crud.uuid.UuidSuffixSettings;
import edu.stanford.bmir.protege.web.shared.form.EntityFormSelector;
import edu.stanford.bmir.protege.web.shared.form.FormDescriptor;
import edu.stanford.bmir.protege.web.shared.form.FormId;
import edu.stanford.bmir.protege.web.shared.issues.Comment;
import edu.stanford.bmir.protege.web.shared.issues.CommentId;
import edu.stanford.bmir.protege.web.shared.issues.EntityDiscussionThread;
import edu.stanford.bmir.protege.web.shared.issues.Status;
import edu.stanford.bmir.protege.web.shared.issues.ThreadId;
import edu.stanford.bmir.protege.web.shared.lang.DisplayNameSettings;
import edu.stanford.bmir.protege.web.shared.lang.LanguageMap;
import edu.stanford.bmir.protege.web.shared.match.criteria.CompositeRootCriteria;
import edu.stanford.bmir.protege.web.shared.match.criteria.EntityIsDeprecatedCriteria;
import edu.stanford.bmir.protege.web.shared.match.criteria.EntityTypeIsOneOfCriteria;
import edu.stanford.bmir.protege.web.shared.match.criteria.HierarchyFilterType;
import edu.stanford.bmir.protege.web.shared.match.criteria.MultiMatchType;
import edu.stanford.bmir.protege.web.shared.match.criteria.SubClassOfCriteria;
import edu.stanford.bmir.protege.web.shared.perspective.PerspectiveDescriptor;
import edu.stanford.bmir.protege.web.shared.perspective.PerspectiveId;
import edu.stanford.bmir.protege.web.shared.project.PrefixDeclarations;
import edu.stanford.bmir.protege.web.shared.project.ProjectDetails;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.search.EntitySearchFilter;
import edu.stanford.bmir.protege.web.shared.search.EntitySearchFilterId;
import edu.stanford.bmir.protege.web.shared.shortform.AnnotationAssertionDictionaryLanguage;
import edu.stanford.bmir.protege.web.shared.tag.Tag;
import edu.stanford.bmir.protege.web.shared.tag.TagId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.bmir.protege.web.shared.watches.WatchType;
import edu.stanford.bmir.protege.web.shared.webhook.ProjectWebhook;
import edu.stanford.bmir.protege.web.shared.webhook.ProjectWebhookEventType;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import org.mockito.stubbing.Answer;
import org.mongodb.morphia.Morphia;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * One or two samples of every Mongo collection the ported application reads and writes (docs/01 §5.3), produced by
 * the legacy persistence code itself: Morphia 1.3.2 with the legacy converters ({@code MorphiaProvider}), the legacy
 * Jackson {@code ObjectMapperProvider} that the raw-driver repositories pass documents through, and the hand-written
 * {@code UserRecordConverter}. The values are fixed, so the result is the same on every run.
 * <p>
 * Each document is rendered as it would be stored: the driver writes {@code _id} first and generates an
 * {@code ObjectId} when the document has none (here a fixed one). {@code ProjectAccess} is the exception: the
 * legacy code only ever upserts it with {@code $inc}/{@code $set}, so its sample is the document that update creates.
 * <p>
 * The rendering is canonical Extended JSON, one document per line, as {@code mongoexport --jsonFormat=canonical}
 * writes it; it keeps the BSON types (Int32, Int64, Date, ObjectId) and the field order.
 */
final class LegacyMongoDocuments {

    static final JsonWriterSettings CANONICAL = JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED).build();

    static final ProjectId PIZZA_PROJECT = ProjectId.get("2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81");

    static final ProjectId WINE_PROJECT = ProjectId.get("7c1f0e9d-8b2a-4d3c-a1e5-0f9e8d7c6b52");

    static final UserId ALICE = UserId.getUserId("alice");

    static final UserId BOB = UserId.getUserId("bob");

    private static final String PIZZA = "http://www.co-ode.org/ontologies/pizza/pizza.owl#";

    private static final long CREATED = 1_600_000_000_000L;

    private static final long MODIFIED = 1_650_000_000_123L;

    private static final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private final ObjectMapper objectMapper = new ObjectMapperProvider().get();

    private final Morphia morphia = new MorphiaProvider(new UserIdConverter(),
                                                        new OWLEntityConverter(dataFactory),
                                                        new ProjectIdConverter(),
                                                        new ThreadIdConverter(),
                                                        new CommentIdConverter(),
                                                        new CollectionIdConverter(),
                                                        new FormIdConverter(),
                                                        new TagIdConverter(),
                                                        new ColorConverter()).get();

    private int nextObjectId = 1;

    /**
     * The canonical Extended JSON lines of every collection, by collection name.
     */
    Map<String, List<String>> all() throws IOException {
        var collections = new LinkedHashMap<String, List<String>>();
        collections.put("Users", users());
        collections.put("UserActivity", userActivity());
        collections.put("ProjectDetails", projectDetails());
        collections.put("ProjectAccess", projectAccess());
        collections.put("RoleAssignments", roleAssignments());
        collections.put("PrefixDeclarations", prefixDeclarations());
        collections.put("EntityCrudKitSettings", entityCrudKitSettings());
        collections.put("PerspectiveDescriptors", perspectiveDescriptors());
        collections.put("PerspectiveLayouts", perspectiveLayouts());
        collections.put("Forms", forms());
        collections.put("FormSelectors", formSelectors());
        collections.put("Tags", tags());
        collections.put("EntityTags", entityTags());
        collections.put("Watches", watches());
        collections.put("EntityDiscussionThreads", entityDiscussionThreads());
        collections.put("ProjectWebhook", projectWebhooks());
        collections.put("UserApiKeys", userApiKeys());
        collections.put("ApplicationPreferences", applicationPreferences());
        collections.put("EntitySearchFilters", entitySearchFilters());
        return collections;
    }

    // UserRecordRepository: hand-written UserRecordConverter over the raw driver

    private List<String> users() {
        var converter = new UserRecordConverter();
        var salt = new Salt(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        var digest = new SaltedPasswordDigest(new byte[]{(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef,
                0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef,
                0x10, 0x32, 0x54, 0x76});
        return List.of(
                stored(converter.toDocument(new UserRecord(ALICE, "Alice Liddell", "alice@example.org",
                                                           "https://example.org/avatars/alice.png", salt, digest))),
                // The converter leaves out an empty avatar URL.
                stored(converter.toDocument(new UserRecord(BOB, "Bob", "", "", salt, digest))));
    }

    // UserActivityManager: Morphia

    private List<String> userActivity() {
        return List.of(
                morphia(new UserActivityRecord(ALICE, MODIFIED, CREATED, List.of(
                        new RecentProjectRecord(PIZZA_PROJECT, MODIFIED),
                        new RecentProjectRecord(WINE_PROJECT, CREATED)))),
                morphia(UserActivityRecord.get(BOB)));
    }

    // ProjectDetailsRepository: Jackson

    private List<String> projectDetails() {
        var label = AnnotationAssertionDictionaryLanguage.get(OWLRDFVocabulary.RDFS_LABEL.getIRI(), "en");
        var chineseLabel = AnnotationAssertionDictionaryLanguage.get(OWLRDFVocabulary.RDFS_LABEL.getIRI(), "zh");
        var displayNameSettings = DisplayNameSettings.get(ImmutableList.of(label, chineseLabel),
                                                          ImmutableList.of());
        return List.of(
                jackson(ProjectDetails.get(PIZZA_PROJECT, "Pizza", "The pizza ontology", ALICE, false, label,
                                           displayNameSettings, CREATED, ALICE, MODIFIED, BOB)),
                jackson(ProjectDetails.get(WINE_PROJECT, "Wine", "", BOB, true, label,
                                           DisplayNameSettings.empty(), CREATED, BOB, CREATED, BOB)));
    }

    // ProjectAccessManagerImpl.logProjectAccess: upsert {projectId, userId} with $inc count and $set accessed

    private List<String> projectAccess() {
        return List.of(stored(new Document("projectId", PIZZA_PROJECT.getId())
                                      .append("userId", ALICE.getUserName())
                                      .append("accessed", new Date(MODIFIED))
                                      .append("count", 3)));
    }

    // AccessManagerImpl.setAssignedRoles: Morphia; closures computed as the legacy access manager does

    private List<String> roleAssignments() {
        return List.of(
                morphia(roleAssignment(ALICE.getUserName(), PIZZA_PROJECT.getId(), BuiltInRole.PROJECT_MANAGER)),
                morphia(roleAssignment(BOB.getUserName(), PIZZA_PROJECT.getId(), BuiltInRole.CAN_COMMENT)),
                // Application roles have no project id and the "any signed-in user" subject has no user name;
                // Morphia leaves the null fields out.
                morphia(roleAssignment(ALICE.getUserName(), null, BuiltInRole.SYSTEM_ADMIN)),
                morphia(roleAssignment(null, null, BuiltInRole.PROJECT_CREATOR, BuiltInRole.PROJECT_UPLOADER)));
    }

    private static RoleAssignment roleAssignment(String userName, String projectId, BuiltInRole... roles) {
        var roleOracle = RoleOracleImpl.get();
        var roleIds = Arrays.stream(roles).map(BuiltInRole::getRoleId).collect(toList());
        var roleClosure = roleIds.stream()
                                 .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                                 .map(role -> role.getRoleId().getId())
                                 .collect(toList());
        var actionClosure = roleIds.stream()
                                   .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                                   .flatMap(role -> role.getActions().stream())
                                   .map(action -> action.getId())
                                   .sorted()
                                   .collect(toList());
        return new RoleAssignment(userName, projectId, roleIds.stream().map(RoleId::getId).collect(toList()),
                                  roleClosure, actionClosure);
    }

    // PrefixDeclarationsStore: Jackson

    private List<String> prefixDeclarations() {
        return List.of(jackson(PrefixDeclarations.get(PIZZA_PROJECT, ImmutableMap.of(
                "pizza:", PIZZA,
                "owl:", "http://www.w3.org/2002/07/owl#",
                ":", PIZZA))));
    }

    // ProjectEntityCrudKitSettingsRepository: Jackson

    private List<String> entityCrudKitSettings() {
        var settings = EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(PIZZA, ImmutableList.of()),
                                                 UuidSuffixSettings.get());
        return List.of(jackson(ProjectEntityCrudKitSettings.get(PIZZA_PROJECT, settings)));
    }

    // PerspectiveDescriptorRepositoryImpl: Jackson

    private List<String> perspectiveDescriptors() {
        var classes = PerspectiveDescriptor.get(PerspectiveId.get("69df8fa8-4f84-499e-9341-28eb5085c40b"),
                                                LanguageMap.of("en", "Classes"), true);
        var comments = PerspectiveDescriptor.get(PerspectiveId.get("0e1a2b3c-4d5e-4f60-8172-839405a6b7c8"),
                                                 LanguageMap.of("en", "Comments"), false);
        return List.of(
                // The application-wide record has neither a project nor a user.
                jackson(PerspectiveDescriptorsRecord.get(ImmutableList.of(classes, comments))),
                jackson(PerspectiveDescriptorsRecord.get(PIZZA_PROJECT, ALICE, ImmutableList.of(classes))));
    }

    // PerspectiveLayoutRepositoryImpl: Jackson; the layout is a widgetmap Node tree

    private List<String> perspectiveLayouts() throws IOException {
        var withLayout = objectMapper.readValue("""
                {
                  "projectId": "%s",
                  "userId": "alice",
                  "perspectiveId": "69df8fa8-4f84-499e-9341-28eb5085c40b",
                  "layout": {
                    "@type": "ParentNode",
                    "direction": "row",
                    "children": [
                      {
                        "weight": 0.3,
                        "node": {
                          "@type": "LeafNode",
                          "properties": {"portlet": {"@type": "String", "value": "portlets.ClassHierarchy"}}
                        }
                      },
                      {
                        "weight": 0.7,
                        "node": {
                          "@type": "LeafNode",
                          "properties": {"portlet": {"@type": "String", "value": "portlets.ClassEditor"}}
                        }
                      }
                    ]
                  }
                }
                """.formatted(PIZZA_PROJECT.getId()), PerspectiveLayoutRecord.class);
        var withoutLayout = objectMapper.readValue("""
                {"perspectiveId": "0e1a2b3c-4d5e-4f60-8172-839405a6b7c8"}
                """, PerspectiveLayoutRecord.class);
        return List.of(jackson(withLayout), jackson(withoutLayout));
    }

    // EntityFormRepositoryImpl: Jackson; the descriptor is the current-format form used by the P0-10 tests

    private List<String> forms() throws IOException {
        FormDescriptor descriptor;
        try (InputStream in = LegacyMongoDocuments.class.getResourceAsStream("/forms/pizza-form.json")) {
            descriptor = objectMapper.readValue(in, FormDescriptor.class);
        }
        return List.of(jackson(FormDescriptorRecord.get(PIZZA_PROJECT, descriptor, 0)));
    }

    // EntityFormSelectorRepositoryImpl: Jackson

    private List<String> formSelectors() {
        var criteria = CompositeRootCriteria.get(ImmutableList.of(
                SubClassOfCriteria.get(cls("Pizza"), HierarchyFilterType.ALL),
                EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.NAMED_INDIVIDUAL))), MultiMatchType.ALL);
        return List.of(jackson(EntityFormSelector.get(PIZZA_PROJECT, criteria,
                                                      FormId.get("8b3c4d5e-6f70-4182-93a4-b5c6d7e8f901"))));
    }

    // TagRepositoryImpl: Jackson

    private List<String> tags() {
        var deprecated = Tag.get(TagId.getId("5d6e7f80-91a2-4b3c-8d4e-5f60718293a4"), PIZZA_PROJECT, "Deprecated",
                                 "Entities that should no longer be used", Color.getHex("#ffffff"),
                                 Color.getHex("#c0392b"), ImmutableList.of(EntityIsDeprecatedCriteria.get()));
        var review = Tag.get(TagId.getId("6e7f8091-a2b3-4c4d-9e5f-60718293a4b5"), PIZZA_PROJECT, "Needs review",
                             "", Color.getHex("#000000"), Color.getHex("#f1c40f"), ImmutableList.of());
        return List.of(jackson(deprecated), jackson(review));
    }

    // EntityTagsRepositoryImpl: Morphia; the entity goes through OWLEntityConverter

    private List<String> entityTags() {
        return List.of(morphia(new EntityTags(PIZZA_PROJECT, cls("Margherita"), List.of(
                TagId.getId("5d6e7f80-91a2-4b3c-8d4e-5f60718293a4"),
                TagId.getId("6e7f8091-a2b3-4c4d-9e5f-60718293a4b5")))));
    }

    // WatchRecordRepositoryImpl: Morphia

    private List<String> watches() {
        return List.of(
                morphia(new WatchRecord(PIZZA_PROJECT, ALICE, cls("Pizza"), WatchType.BRANCH)),
                morphia(new WatchRecord(PIZZA_PROJECT, BOB,
                                        dataFactory.getOWLObjectProperty(IRI.create(PIZZA + "hasTopping")),
                                        WatchType.ENTITY)));
    }

    // EntityDiscussionThreadRepository: Morphia; comments are embedded

    private List<String> entityDiscussionThreads() {
        var first = new Comment(CommentId.fromString("c0a80101-0000-4000-8000-000000000001"), ALICE, CREATED,
                                Optional.of(MODIFIED), "Is @bob sure about this?",
                                "<p>Is <a href=\"#bob\">@bob</a> sure about this?</p>");
        var second = new Comment(CommentId.fromString("c0a80101-0000-4000-8000-000000000002"), BOB, MODIFIED,
                                 Optional.empty(), "Yes.", "<p>Yes.</p>");
        return List.of(
                morphia(new EntityDiscussionThread(new ThreadId("b1c2d3e4-f5a6-4b7c-8d9e-0f1a2b3c4d5e"),
                                                   PIZZA_PROJECT, cls("AmericanHot"), Status.OPEN,
                                                   ImmutableList.of(first, second))),
                morphia(new EntityDiscussionThread(new ThreadId("c2d3e4f5-a6b7-4c8d-9e0f-1a2b3c4d5e6f"),
                                                   PIZZA_PROJECT,
                                                   dataFactory.getOWLNamedIndividual(IRI.create(PIZZA + "Italy")),
                                                   Status.CLOSED, ImmutableList.of())));
    }

    // WebhookRepositoryImpl: Morphia; the collection is named after the class

    private List<String> projectWebhooks() {
        return List.of(morphia(new ProjectWebhook(PIZZA_PROJECT, "https://hooks.example.org/pizza",
                                                  List.of(ProjectWebhookEventType.PROJECT_CHANGED))));
    }

    // UserApiKeyStoreImpl: Morphia; the user id is the _id

    private List<String> userApiKeys() {
        return List.of(morphia(new UserApiKeys(ALICE, List.of(
                new ApiKeyRecord(ApiKeyId.valueOf("d3e4f5a6-b7c8-4d9e-8f01-2a3b4c5d6e7f"),
                                 HashedApiKey.valueOf(
                                         "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"),
                                 CREATED, "CI pipeline"),
                new ApiKeyRecord(ApiKeyId.valueOf("e4f5a6b7-c8d9-4e0f-9a12-3b4c5d6e7f80"),
                                 HashedApiKey.valueOf(
                                         "60303ae22b998861bce3b28f33eec1be758a213c86c93c076dbe9f558c11c752"),
                                 MODIFIED, "Notebook")))));
    }

    // ApplicationPreferencesStore: Morphia; a single document with the fixed id "Preferences"

    private List<String> applicationPreferences() {
        return List.of(morphia(new ApplicationPreferences("IndustrialOntology", "admin@example.org",
                                                          new ApplicationLocation("https", "ontology.example.org",
                                                                                  "/webprotege", 443),
                                                          Long.MAX_VALUE)));
    }

    // EntitySearchFilterRepositoryImpl: Jackson

    private List<String> entitySearchFilters() {
        return List.of(jackson(EntitySearchFilter.get(
                EntitySearchFilterId.get("f5a6b7c8-d9e0-4f1a-8b23-4c5d6e7f8091"), PIZZA_PROJECT,
                LanguageMap.of("en", "Deprecated entities"), EntityIsDeprecatedCriteria.get())));
    }

    /**
     * Every index the legacy persistence code declares, one canonical Extended JSON line per index:
     * {@code {"collection": …, "key": {…}, "unique": …}}. Each legacy repository's own {@code ensureIndexes()} runs
     * against a mocked driver that records the {@code createIndex} calls; for the Morphia entities that is
     * {@code Datastore.ensureIndexes(Class)}, which derives the keys from their {@code @Indexes} annotations.
     * <p>
     * The legacy server did not run all of them: {@code ProjectDetailsRepository.ensureIndexes()} and the
     * {@code ProjectWebhook} indexes are declared but never called at startup.
     */
    List<String> indexes() {
        var recorded = new ArrayList<String>();
        var database = recordingDatabase(recorded);
        new ProjectAccessManagerImpl(database).ensureIndexes();
        new ProjectDetailsRepository(database, objectMapper).ensureIndexes();
        new PerspectiveDescriptorRepositoryImpl(database, objectMapper).ensureIndexes();
        new PerspectiveLayoutRepositoryImpl(database, objectMapper).ensureIndexes();
        new EntityFormRepositoryImpl(objectMapper, database).ensureIndexes();
        new EntityFormSelectorRepositoryImpl(database, objectMapper).ensureIndexes();
        new TagRepositoryImpl(PIZZA_PROJECT, database, objectMapper).ensureIndexes();
        new EntitySearchFilterRepositoryImpl(database, objectMapper).ensureIndexes();

        var mongoClient = mock(MongoClient.class);
        when(mongoClient.getDatabase(anyString())).thenReturn(database);
        var datastore = morphia.createDatastore(mongoClient, "webprotege");
        new UserActivityManager(datastore).ensureIndexes();
        new WatchRecordRepositoryImpl(datastore).ensureIndexes();
        new EntityTagsRepositoryImpl(PIZZA_PROJECT, datastore).ensureIndexes();
        new UserApiKeyStoreImpl(datastore).ensureIndexes();
        datastore.ensureIndexes(EntityDiscussionThread.class);
        datastore.ensureIndexes(RoleAssignment.class);
        datastore.ensureIndexes(ProjectWebhook.class);
        datastore.ensureIndexes(ApplicationPreferences.class);
        return recorded;
    }

    @SuppressWarnings("unchecked")
    private static MongoDatabase recordingDatabase(List<String> recorded) {
        var database = mock(MongoDatabase.class);
        Answer<MongoCollection<?>> collection = invocation -> {
            String name = invocation.getArgument(0);
            MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
            Answer<String> createIndex = call -> {
                var keys = ((Bson) call.getArgument(0)).toBsonDocument(BsonDocument.class,
                                                                       MongoClient.getDefaultCodecRegistry());
                var unique = call.getArguments().length > 1
                        && Boolean.TRUE.equals(((IndexOptions) call.getArgument(1)).isUnique());
                recorded.add(new Document("collection", name).append("key", keys)
                                                             .append("unique", unique)
                                                             .toJson(CANONICAL));
                return "";
            };
            when(mongoCollection.createIndex(any(Bson.class))).thenAnswer(createIndex);
            when(mongoCollection.createIndex(any(Bson.class), any(IndexOptions.class))).thenAnswer(createIndex);
            return mongoCollection;
        };
        when(database.getCollection(anyString())).thenAnswer(collection);
        when(database.getCollection(anyString(), any(Class.class))).thenAnswer(collection);
        when(database.getCodecRegistry()).thenReturn(MongoClient.getDefaultCodecRegistry());
        return database;
    }

    private static OWLClass cls(String name) {
        return dataFactory.getOWLClass(IRI.create(PIZZA + name));
    }

    private String morphia(Object entity) {
        var dbObject = (BasicDBObject) morphia.toDBObject(entity);
        return stored(Document.parse(dbObject.toJson(CANONICAL)));
    }

    private String jackson(Object record) {
        return stored(objectMapper.convertValue(record, Document.class));
    }

    /**
     * The document as the driver stores it: {@code _id} first, a fresh {@code ObjectId} when there is none.
     */
    private String stored(Document document) {
        var reparsed = Document.parse(document.toJson(CANONICAL));
        var stored = new Document("_id", reparsed.containsKey("_id") ? reparsed.get("_id") : nextObjectId());
        reparsed.forEach((key, value) -> {
            if (!key.equals("_id")) {
                stored.append(key, value);
            }
        });
        return stored.toJson(CANONICAL);
    }

    private ObjectId nextObjectId() {
        return new ObjectId(String.format("5f0c0c0c0c0c0c0c0c%06x", nextObjectId++));
    }
}
