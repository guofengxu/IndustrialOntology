package org.industrial.ontology.app.project.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectPersistenceIT {

    private static final ProjectId P1 = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId P2 = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static MongoPersistenceTestContext context;

    private MongoProjectDetailsRepository details;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        details = context.bean(MongoProjectDetailsRepository.class);
    }

    @Test
    void shouldFindProjectsByIdAndOwner() {
        details.save(project(P1, "One", ALICE));
        details.save(project(P2, "Two", BOB));

        assertThat(details.containsProject(P1)).isTrue();
        assertThat(details.containsProjectWithOwner(P1, ALICE)).isTrue();
        assertThat(details.containsProjectWithOwner(P1, BOB)).isFalse();
        assertThat(details.findByOwner(BOB)).extracting(ProjectDetails::getDisplayName).containsExactly("Two");

        details.delete(P1);
        assertThat(details.containsProject(P1)).isFalse();
        assertThat(details.findOne(P1)).isEmpty();
    }

    @Test
    void shouldSeeChangesThroughTheCache() {
        details.save(project(P1, "One", ALICE));
        assertThat(details.findOne(P1)).map(ProjectDetails::isInTrash).contains(false);

        details.setInTrash(P1, true);
        assertThat(details.findOne(P1)).map(ProjectDetails::isInTrash).contains(true);

        details.setModified(P1, 5000, BOB);
        var modified = details.findOne(P1).orElseThrow();
        assertThat(modified.getLastModifiedAt()).isEqualTo(5000);
        assertThat(modified.getLastModifiedBy()).isEqualTo(BOB);
        // Stored in the ISO form that ProjectDetails is written with.
        var stored = context.database().getCollection(MongoProjectDetailsRepository.COLLECTION).find().first();
        assertThat(stored.getString(ProjectDetails.MODIFIED_AT)).isEqualTo("1970-01-01T00:00:05Z");

        details.save(project(P1, "Renamed", ALICE));
        assertThat(details.findOne(P1)).map(ProjectDetails::getDisplayName).contains("Renamed");
    }

    @Test
    void shouldGiveTheDisplayNameLanguagesOfTheProject() {
        var english = DictionaryLanguage.rdfsLabel("en");
        details.save(project(P1, "One", ALICE).withDefaultDisplayNameSettings(
                DisplayNameSettings.get(ImmutableList.of(english), ImmutableList.of())));

        assertThat(details.getDisplayNameLanguages(P1)).containsExactly(english);
        assertThat(details.getDisplayNameLanguages(P2)).isEmpty();
    }

    @Test
    void shouldCountProjectAccesses() {
        var access = context.bean(ProjectAccessRepository.class);

        access.logProjectAccess(P1, ALICE, 1000);
        access.logProjectAccess(P1, ALICE, 2000);
        access.logProjectAccess(P1, BOB, 3000);

        assertThat(access.findAccess(P1, ALICE)).hasValueSatisfying(document -> {
            assertThat(document.count()).isEqualTo(2);
            assertThat(document.accessed()).isEqualTo(Instant.ofEpochMilli(2000));
        });
        assertThat(access.findAccess(P1, BOB)).map(ProjectAccessDocument::count).contains(1);
        assertThat(access.findAccess(P2, ALICE)).isEmpty();
    }

    @Test
    void shouldGiveNoPrefixesForAProjectWithoutAny() {
        var store = context.bean(MongoPrefixDeclarationsStore.class);
        assertThat(store.find(P1)).isEqualTo(PrefixDeclarations.get(P1));

        store.save(PrefixDeclarations.get(P1, ImmutableMap.of("ex:", "http://example.org/")));
        store.save(PrefixDeclarations.get(P1, ImmutableMap.of("ex:", "http://example.com/")));

        assertThat(store.find(P1).getPrefixes()).containsExactly(java.util.Map.entry("ex:", "http://example.com/"));
        assertThat(context.database().getCollection(MongoPrefixDeclarationsStore.COLLECTION).countDocuments())
                .isEqualTo(1);
    }

    private static ProjectDetails project(ProjectId projectId, String name, UserId owner) {
        return ProjectDetails.get(projectId, name, "", owner, false, DictionaryLanguage.rdfsLabel(""),
                                  DisplayNameSettings.empty(), 1000, owner, 1000, owner);
    }
}
