package org.industrial.ontology.app.persistence;

import org.industrial.ontology.app.access.persistence.RoleAssignmentDocument;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesDocument;
import org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument;
import org.industrial.ontology.app.form.persistence.MongoEntityFormRepository;
import org.industrial.ontology.app.form.persistence.MongoEntityFormSelectorRepository;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument;
import org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorRepository;
import org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessDocument;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.tag.persistence.EntityTagsDocument;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.app.user.persistence.UserActivityDocument;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.watch.persistence.WatchDocument;
import org.industrial.ontology.app.webhook.persistence.WebhookDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyCollectionTest {

    /**
     * The index catalogue is exactly what the legacy code declares ({@code indexes.json}, written by the legacy
     * repositories' {@code ensureIndexes()} in wp-legacy-compat): same keys in the same order, same uniqueness.
     */
    @Test
    void indexesShouldBeTheLegacyIndexes() {
        var legacy = LegacyMongoSamples.documents("indexes")
                                       .stream()
                                       .map(index -> describe(index.getString("collection"),
                                                              List.copyOf(index.get("key", org.bson.Document.class)
                                                                               .keySet()),
                                                              index.getBoolean("unique")))
                                       .toList();
        var declared = Stream.of(LegacyCollection.values())
                             .flatMap(collection -> collection.indexes()
                                                              .stream()
                                                              .map(index -> describe(collection.collectionName(),
                                                                                     index.keys(),
                                                                                     index.unique())))
                             .toList();
        assertThat(declared).hasSize(16).containsExactlyInAnyOrderElementsOf(legacy);
    }

    /**
     * Every repository and document class uses one of the 19 legacy collection names, and every legacy collection
     * has one.
     */
    @Test
    void documentsAndRepositoriesShouldCoverTheLegacyCollections() {
        var used = List.of(UserRecordDocument.COLLECTION,
                           UserActivityDocument.COLLECTION,
                           MongoProjectDetailsRepository.COLLECTION,
                           ProjectAccessDocument.COLLECTION,
                           RoleAssignmentDocument.COLLECTION,
                           MongoPrefixDeclarationsStore.COLLECTION,
                           MongoEntityCrudKitSettingsRepository.COLLECTION,
                           PerspectiveDescriptorRepository.COLLECTION,
                           PerspectiveLayoutRepository.COLLECTION,
                           MongoEntityFormRepository.COLLECTION,
                           MongoEntityFormSelectorRepository.COLLECTION,
                           TagRepository.COLLECTION,
                           EntityTagsDocument.COLLECTION,
                           WatchDocument.COLLECTION,
                           DiscussionThreadDocument.COLLECTION,
                           WebhookDocument.COLLECTION,
                           UserApiKeysDocument.COLLECTION,
                           ApplicationPreferencesDocument.COLLECTION,
                           EntitySearchFilterRepository.COLLECTION);
        assertThat(used).containsExactlyElementsOf(Stream.of(LegacyCollection.values())
                                                         .map(LegacyCollection::collectionName)
                                                         .toList());
    }

    @Test
    void onlyTheMorphiaCollectionsWithEntitiesShouldHaveAnEntityField() {
        assertThat(Stream.of(LegacyCollection.values()).filter(collection -> collection.entityField().isPresent()))
                .containsExactly(LegacyCollection.ENTITY_TAGS,
                                 LegacyCollection.WATCHES,
                                 LegacyCollection.ENTITY_DISCUSSION_THREADS)
                .allSatisfy(collection -> assertThat(collection.storage())
                        .isEqualTo(LegacyCollection.Storage.MORPHIA));
    }

    private static String describe(String collection, List<String> keys, boolean unique) {
        return collection + " " + keys + (unique ? " unique" : "");
    }
}
