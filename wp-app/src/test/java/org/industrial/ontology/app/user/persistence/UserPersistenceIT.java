package org.industrial.ontology.app.user.persistence;

import org.bson.Document;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserPersistenceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final ProjectId P1 = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId P2 = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final ProjectId P3 = ProjectId.get("33333333-3333-4333-8333-333333333333");

    private static MongoPersistenceTestContext context;

    private UserRecordRepository users;

    private UserActivityRepository activity;

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
        users = context.bean(UserRecordRepository.class);
        activity = context.bean(UserActivityRepository.class);
    }

    @Test
    void shouldFindUsersByIdAndByEmailAddress() {
        users.save(UserRecordDocument.of(ALICE, "Alice", "alice@example.org", ""));

        assertThat(users.findOne(ALICE)).map(UserRecordDocument::realName).contains("Alice");
        assertThat(users.findOneByEmailAddress("alice@example.org")).map(UserRecordDocument::getUserId)
                                                                    .contains(ALICE);
        assertThat(users.findOneByEmailAddress("bob@example.org")).isEmpty();

        users.delete(ALICE);
        assertThat(users.findOne(ALICE)).isEmpty();
    }

    @Test
    void shouldStoreUsersCreatedFromALoginWithoutLegacyPasswordOrEmptyAvatar() {
        users.save(UserRecordDocument.of(ALICE, "Alice", "alice@example.org", ""));

        var stored = context.database().getCollection("Users").find().first();
        assertThat(List.copyOf(stored.keySet())).containsExactly("_id", "realName", "emailAddress");
    }

    @Test
    void shouldMatchUserNamesLiterallyAndIgnoringCase() {
        for (var name : List.of("Alice", "malice", "bob", "a.b", "axb")) {
            users.save(UserRecordDocument.of(UserId.getUserId(name), name, "", ""));
        }

        assertThat(users.findByUserIdContainingIgnoreCase("ALI", 10)).extracting(UserId::getUserName)
                                                                       .containsExactlyInAnyOrder("Alice", "malice");
        assertThat(users.findByUserIdContainingIgnoreCase("ali", 1)).hasSize(1);
        // "." is not a pattern: it matches only a dot.
        assertThat(users.findByUserIdContainingIgnoreCase("a.b", 10)).extracting(UserId::getUserName)
                                                                       .containsExactly("a.b");
    }

    @Test
    void shouldCreateACompleteActivityRecordOnTheFirstLogin() {
        activity.setLastLogin(ALICE, 1000);

        assertThat(activity.getUserActivityRecord(ALICE)).contains(new UserActivityDocument(
                "alice", Instant.ofEpochMilli(1000), Instant.EPOCH, List.of()));

        activity.setLastLogout(ALICE, 2000);
        assertThat(activity.getUserActivityRecord(ALICE)).contains(new UserActivityDocument(
                "alice", Instant.ofEpochMilli(1000), Instant.ofEpochMilli(2000), List.of()));
        var stored = context.database().getCollection("UserActivity").find().first();
        assertThat(stored.get("lastLogin")).isInstanceOf(java.util.Date.class);
    }

    @Test
    void shouldMoveTheLatestProjectToTheFrontOfTheRecentProjects() {
        activity.addRecentProject(ALICE, P1, 1000);
        activity.addRecentProject(ALICE, P2, 2000);
        activity.addRecentProject(ALICE, P3, 3000);
        activity.addRecentProject(ALICE, P1, 4000);

        assertThat(activity.getUserActivityRecord(ALICE).orElseThrow().recentProjects())
                .containsExactly(UserActivityDocument.RecentProject.of(P1, 4000),
                                 UserActivityDocument.RecentProject.of(P3, 3000),
                                 UserActivityDocument.RecentProject.of(P2, 2000));
    }

    @Test
    void shouldRecordNothingForTheGuest() {
        activity.setLastLogin(UserId.getGuest(), 1000);
        activity.addRecentProject(UserId.getGuest(), P1, 1000);
        activity.save(UserActivityDocument.empty(UserId.getGuest()));

        assertThat(context.database().getCollection("UserActivity").countDocuments(new Document())).isZero();
        assertThat(activity.getUserActivityRecord(UserId.getGuest())).isEmpty();
    }
}
