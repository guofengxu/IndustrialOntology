package org.industrial.ontology.app.persistence;

import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

/**
 * The 19 collections of the legacy {@code webprotege} database that the ported application reads and writes
 * (docs/01 §5.3), with how the legacy code stored them and the indexes it declared.
 * <p>
 * The index list is what the legacy repositories' {@code ensureIndexes()} methods and the Morphia {@code @Indexes}
 * annotations declare, pinned by {@code legacy-mongo/indexes.json}. The legacy server did not create all of them: it
 * never called {@code ProjectDetailsRepository.ensureIndexes()} and never ensured the {@code ProjectWebhook} indexes,
 * so a legacy database may lack those two. {@link MongoIndexes} creates the missing ones.
 */
public enum LegacyCollection {

    USERS("Users", Storage.DRIVER),
    USER_ACTIVITY("UserActivity", Storage.MORPHIA),
    PROJECT_DETAILS("ProjectDetails", Storage.JACKSON,
                    index("_id", "displayName")),
    PROJECT_ACCESS("ProjectAccess", Storage.DRIVER,
                   index("projectId", "userId")),
    ROLE_ASSIGNMENTS("RoleAssignments", Storage.MORPHIA,
                     unique("userName", "projectId")),
    PREFIX_DECLARATIONS("PrefixDeclarations", Storage.JACKSON),
    ENTITY_CRUD_KIT_SETTINGS("EntityCrudKitSettings", Storage.JACKSON),
    PERSPECTIVE_DESCRIPTORS("PerspectiveDescriptors", Storage.JACKSON,
                            unique("projectId", "userId", "perspectives")),
    PERSPECTIVE_LAYOUTS("PerspectiveLayouts", Storage.JACKSON,
                        unique("projectId", "userId", "perspectiveId")),
    FORMS("Forms", Storage.JACKSON,
          unique("projectId", "formDescriptor.formId")),
    FORM_SELECTORS("FormSelectors", Storage.JACKSON),
    TAGS("Tags", Storage.JACKSON,
         unique("projectId", "label")),
    ENTITY_TAGS("EntityTags", Storage.MORPHIA,
                unique("projectId", "entity"),
                index("tags")),
    WATCHES("Watches", Storage.MORPHIA,
            unique("projectId", "userId", "entity")),
    ENTITY_DISCUSSION_THREADS("EntityDiscussionThreads", Storage.MORPHIA,
                              index("projectId", "entity", "status"),
                              unique("comments._id")),
    PROJECT_WEBHOOK("ProjectWebhook", Storage.MORPHIA,
                    unique("projectId", "subscribedToEvents")),
    USER_API_KEYS("UserApiKeys", Storage.MORPHIA,
                  unique("apiKeys.apiKeyId"),
                  unique("apiKeys.apiKey")),
    APPLICATION_PREFERENCES("ApplicationPreferences", Storage.MORPHIA),
    ENTITY_SEARCH_FILTERS("EntitySearchFilters", Storage.JACKSON,
                          index("projectId"));

    /**
     * How the legacy code wrote a collection, which decides where migrate-mongo looks for Morphia's
     * {@code className} field.
     */
    public enum Storage {

        /** Morphia entities: {@code className} may appear at the top level and in embedded objects. */
        MORPHIA,

        /**
         * {@code objectMapper.convertValue(record, Document.class)} through the raw driver. Nested values are Jackson
         * data and may legitimately contain fields such as {@code _class} (a Jackson type id), so only a top-level
         * {@code className} is Morphia's.
         */
        JACKSON,

        /** Documents built by hand with the raw driver. */
        DRIVER
    }

    /**
     * An index as the legacy code declared it: ascending keys, in order, with Mongo's default name.
     */
    public record Index(@Nonnull List<String> keys, boolean unique) {

        public Index {
            keys = List.copyOf(keys);
        }

        @Nonnull
        public Document keyDocument() {
            var document = new Document();
            keys.forEach(key -> document.append(key, 1));
            return document;
        }
    }

    private final String collectionName;

    private final Storage storage;

    private final List<Index> indexes;

    LegacyCollection(String collectionName, Storage storage, Index... indexes) {
        this.collectionName = collectionName;
        this.storage = storage;
        this.indexes = List.of(indexes);
    }

    @Nonnull
    public String collectionName() {
        return collectionName;
    }

    @Nonnull
    public Storage storage() {
        return storage;
    }

    @Nonnull
    public List<Index> indexes() {
        return indexes;
    }

    /**
     * The top-level field that holds an entity in the {@link OwlEntityMongoCodec} format, if the collection has one.
     */
    @Nonnull
    public Optional<String> entityField() {
        return switch (this) {
            case ENTITY_TAGS, WATCHES, ENTITY_DISCUSSION_THREADS -> Optional.of("entity");
            default -> Optional.empty();
        };
    }

    private static Index index(String... keys) {
        return new Index(List.of(keys), false);
    }

    private static Index unique(String... keys) {
        return new Index(List.of(keys), true);
    }
}
