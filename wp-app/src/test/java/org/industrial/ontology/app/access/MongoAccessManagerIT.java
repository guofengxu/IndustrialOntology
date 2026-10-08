package org.industrial.ontology.app.access;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.industrial.ontology.app.access.persistence.RoleAssignmentDocument;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.persistence.LegacyMongoSamples;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The access manager on the {@code RoleAssignments} collection. {@link Legacy} is the legacy
 * {@code AccessManagerImpl_IT}, converted; the other tests cover what the legacy test did not.
 */
class MongoAccessManagerIT {

    private static final ProjectId PROJECT = ProjectId.get("2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static MongoPersistenceTestContext context;

    private AccessManager manager;

    private MongoCollection<Document> collection;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithServices();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        manager = context.bean(AccessManager.class);
        collection = context.database().getCollection(RoleAssignmentDocument.COLLECTION);
    }

    private static Set<RoleId> roles(BuiltInRole... roles) {
        return Set.copyOf(Arrays.stream(roles).map(BuiltInRole::getRoleId).toList());
    }

    @Nested
    class Legacy {

        private static final String THE_USER_NAME = "The User";

        private final Document userQuery = new Document("userName", THE_USER_NAME);

        private Document storedDocument;

        @BeforeEach
        void assignCanComment() {
            manager.setAssignedRoles(Subject.forUser(THE_USER_NAME), ApplicationResource.get(),
                                     roles(BuiltInRole.CAN_COMMENT));
            storedDocument = collection.find(userQuery).first();
        }

        @Test
        void shouldStoreAssignedRoles() {
            assertThat(storedDocument).isNotNull();
            assertThat(storedDocument.getList("assignedRoles", String.class)).contains("CanComment");
        }

        @Test
        void shouldStoreRoleClosure() {
            assertThat(storedDocument.getList("roleClosure", String.class)).contains("CanView");
        }

        @Test
        void shouldStoreActionClosure() {
            assertThat(storedDocument.getList("actionClosure", String.class)).contains("ViewProject");
        }

        @Test
        void shouldNotStoreDuplicate() {
            manager.setAssignedRoles(Subject.forUser(THE_USER_NAME), ApplicationResource.get(),
                                     roles(BuiltInRole.CAN_COMMENT));
            assertThat(collection.countDocuments()).isEqualTo(1);
        }

        @Test
        void shouldRebuildRoleClosure() {
            emptyTheClosures();
            manager.rebuild();
            assertThat(collection.find().first().getList("roleClosure", String.class)).contains("CanView");
        }

        @Test
        void shouldRebuildActionClosure() {
            emptyTheClosures();
            manager.rebuild();
            assertThat(collection.find().first().getList("actionClosure", String.class)).contains("ViewProject");
        }

        private void emptyTheClosures() {
            collection.updateOne(userQuery, new Document("$set", new Document("roleClosure", List.of())));
            collection.updateOne(userQuery, new Document("$set", new Document("actionClosure", List.of())));
        }
    }

    /**
     * The four assignments of the legacy sample, made through the manager, are the documents the legacy manager
     * wrote, field for field and in the same order; only the generated {@code _id} differs.
     */
    @Test
    void shouldWriteTheDocumentsThatTheLegacyManagerWrote() {
        manager.setAssignedRoles(Subject.forUser(ALICE), ProjectResource.of(PROJECT),
                                 List.of(BuiltInRole.PROJECT_MANAGER.getRoleId()));
        manager.setAssignedRoles(Subject.forUser(BOB), ProjectResource.of(PROJECT),
                                 List.of(BuiltInRole.CAN_COMMENT.getRoleId()));
        manager.setAssignedRoles(Subject.forUser(ALICE), ApplicationResource.get(),
                                 List.of(BuiltInRole.SYSTEM_ADMIN.getRoleId()));
        manager.setAssignedRoles(Subject.forAnySignedInUser(), ApplicationResource.get(),
                                 List.of(BuiltInRole.PROJECT_CREATOR.getRoleId(),
                                         BuiltInRole.PROJECT_UPLOADER.getRoleId()));

        // In any order: the ids that the server generates for upserts need not increase.
        var stored = withoutIds(collection.find().into(new ArrayList<>()));
        assertThat(stored).containsExactlyInAnyOrderElementsOf(withoutIds(LegacyMongoSamples.documents(
                "RoleAssignments")));
    }

    private static List<String> withoutIds(List<Document> documents) {
        return documents.stream().peek(document -> document.remove("_id")).map(LegacyMongoSamples::canonical)
                        .toList();
    }

    @Test
    void anySignedInUserAssignmentsShouldApplyToSignedInUsersButNotToTheGuest() {
        manager.setAssignedRoles(Subject.forAnySignedInUser(), ProjectResource.of(PROJECT),
                                 roles(BuiltInRole.CAN_VIEW));
        var project = ProjectResource.of(PROJECT);

        assertThat(manager.hasPermission(Subject.forUser(ALICE), project, BuiltInAction.VIEW_PROJECT)).isTrue();
        assertThat(manager.hasPermission(Subject.forAnySignedInUser(), project, BuiltInAction.VIEW_PROJECT)).isTrue();
        assertThat(manager.hasPermission(Subject.forGuestUser(), project, BuiltInAction.VIEW_PROJECT)).isFalse();
        assertThat(manager.hasPermission(Subject.forUser(ALICE), ProjectResource.of(OTHER_PROJECT),
                                         BuiltInAction.VIEW_PROJECT)).isFalse();
    }

    @Test
    void guestAssignmentsShouldApplyToTheGuestOnly() {
        manager.setAssignedRoles(Subject.forGuestUser(), ApplicationResource.get(),
                                 roles(BuiltInRole.ACCOUNT_CREATOR));

        assertThat(manager.hasPermission(Subject.forGuestUser(), ApplicationResource.get(),
                                         BuiltInAction.CREATE_ACCOUNT)).isTrue();
        assertThat(manager.hasPermission(Subject.forUser(ALICE), ApplicationResource.get(),
                                         BuiltInAction.CREATE_ACCOUNT)).isFalse();
    }

    @Test
    void closuresShouldCombineTheUsersAssignmentWithThatOfAnySignedInUser() {
        manager.setAssignedRoles(Subject.forUser(ALICE), ProjectResource.of(PROJECT), roles(BuiltInRole.CAN_EDIT));
        manager.setAssignedRoles(Subject.forAnySignedInUser(), ProjectResource.of(PROJECT),
                                 roles(BuiltInRole.PROJECT_DOWNLOADER));
        var alice = Subject.forUser(ALICE);
        var project = ProjectResource.of(PROJECT);

        assertThat(manager.getAssignedRoles(alice, project)).containsExactly(BuiltInRole.CAN_EDIT.getRoleId());
        assertThat(manager.getRoleClosure(alice, project)).contains(BuiltInRole.CAN_EDIT.getRoleId(),
                                                                    BuiltInRole.CAN_VIEW.getRoleId(),
                                                                    BuiltInRole.PROJECT_DOWNLOADER.getRoleId())
                                                          .doesNotHaveDuplicates();
        assertThat(manager.getActionClosure(alice, project)).contains(BuiltInAction.EDIT_ONTOLOGY.getActionId(),
                                                                      BuiltInAction.DOWNLOAD_PROJECT.getActionId())
                                                            .doesNotContain(BuiltInAction.EDIT_SHARING_SETTINGS
                                                                                    .getActionId());
    }

    @Test
    void requireShouldThrowPermissionDeniedForAMissingPermission() {
        manager.setAssignedRoles(Subject.forUser(BOB), ProjectResource.of(PROJECT), roles(BuiltInRole.CAN_VIEW));

        manager.require(BOB, ProjectResource.of(PROJECT), BuiltInAction.VIEW_PROJECT);
        assertThatThrownBy(() -> manager.require(BOB, ProjectResource.of(PROJECT), BuiltInAction.EDIT_ONTOLOGY))
                .isInstanceOfSatisfying(PermissionDeniedException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("PERMISSION_DENIED");
                    assertThat(e.getStatus()).isEqualTo(403);
                })
                .hasMessage("User bob does not have the EditOntology permission on project " + PROJECT.getId());
        assertThatThrownBy(() -> manager.requireSignedIn(UserId.getGuest()))
                .isInstanceOf(PermissionDeniedException.class);
    }

    /**
     * There is no permission cache: wp-cli changes assignments from another process, so a revocation has to apply to
     * the next check.
     */
    @Test
    void revokedPermissionsShouldStopWorkingAtOnce() {
        var project = ProjectResource.of(PROJECT);
        manager.setAssignedRoles(Subject.forUser(BOB), project, roles(BuiltInRole.CAN_EDIT));
        assertThat(manager.hasPermission(Subject.forUser(BOB), project, BuiltInAction.EDIT_ONTOLOGY)).isTrue();

        // As another process would: straight to the collection, past this manager.
        context.bean(RoleAssignmentRepository.class).setAssignedRoles(BOB.getUserName(), PROJECT.getId(), List.of(),
                                                                      List.of(), List.of());

        assertThat(manager.hasPermission(Subject.forUser(BOB), project, BuiltInAction.EDIT_ONTOLOGY)).isFalse();
        assertThat(manager.getAssignedRoles(Subject.forUser(BOB), project)).isEmpty();
    }

    @Test
    void shouldListTheSubjectsOfAResourceAndTheResourcesOfASubject() {
        manager.setAssignedRoles(Subject.forUser(ALICE), ProjectResource.of(PROJECT), roles(BuiltInRole.CAN_MANAGE));
        manager.setAssignedRoles(Subject.forAnySignedInUser(), ProjectResource.of(PROJECT),
                                 roles(BuiltInRole.CAN_VIEW));
        manager.setAssignedRoles(Subject.forUser(ALICE), ProjectResource.of(OTHER_PROJECT),
                                 roles(BuiltInRole.CAN_VIEW));
        manager.setAssignedRoles(Subject.forUser(ALICE), ApplicationResource.get(), roles(BuiltInRole.SYSTEM_ADMIN));

        assertThat(manager.getSubjectsWithAccessToResource(ProjectResource.of(PROJECT)))
                .containsExactlyInAnyOrder(Subject.forUser(ALICE), Subject.forAnySignedInUser());
        assertThat(manager.getResourcesAccessibleToSubject(Subject.forUser(ALICE),
                                                           BuiltInAction.VIEW_PROJECT.getActionId()))
                .containsExactlyInAnyOrder(ProjectResource.of(PROJECT), ProjectResource.of(OTHER_PROJECT));
        assertThat(manager.getResourcesAccessibleToSubject(Subject.forUser(ALICE),
                                                           BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId()))
                .containsExactly(ApplicationResource.get());
    }

    @Test
    void anEmptyRoleListShouldRemoveEveryPermission() {
        var project = ProjectResource.of(PROJECT);
        manager.setAssignedRoles(Subject.forUser(ALICE), project, roles(BuiltInRole.CAN_VIEW));
        manager.setAssignedRoles(Subject.forUser(ALICE), project, List.of());

        assertThat(manager.getAssignedRoles(Subject.forUser(ALICE), project)).isEmpty();
        assertThat(manager.getActionClosure(Subject.forUser(ALICE), project)).isEmpty();
    }

    /**
     * The roles of the identity provider (the Keycloak admin realm role) count on the application for that user
     * only, and are never stored.
     */
    @Nested
    class ExternalRolesOfTheRequest {

        private AccessManager withExternalRoles;

        @BeforeEach
        void grantAliceSystemAdmin() {
            ExternalRoles externalRoles = userId -> userId.equals(ALICE)
                    ? Set.of(BuiltInRole.SYSTEM_ADMIN.getRoleId()) : Set.of();
            withExternalRoles = new MongoAccessManager(RoleOracle.get(),
                                                       context.bean(RoleAssignmentRepository.class),
                                                       externalRoles);
        }

        @Test
        void shouldGrantTheActionsOfTheRolesOnTheApplication() {
            var alice = Subject.forUser(ALICE);

            assertThat(withExternalRoles.hasPermission(alice, ApplicationResource.get(),
                                                       BuiltInAction.EDIT_APPLICATION_SETTINGS)).isTrue();
            assertThat(withExternalRoles.getActionClosure(alice, ApplicationResource.get()))
                    .contains(BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId(),
                              BuiltInAction.VIEW_ANY_USER_DETAILS.getActionId());
            assertThat(withExternalRoles.getRoleClosure(alice, ApplicationResource.get()))
                    .contains(BuiltInRole.SYSTEM_ADMIN.getRoleId(), BuiltInRole.USER_ADMIN.getRoleId());
        }

        @Test
        void shouldNotGrantAnythingElse() {
            // SystemAdmin has MoveAnyProjectToTrash, but application roles do not apply on a project
            assertThat(withExternalRoles.hasPermission(Subject.forUser(ALICE), ProjectResource.of(PROJECT),
                                                       BuiltInAction.MOVE_ANY_PROJECT_TO_TRASH)).isFalse();
            assertThat(withExternalRoles.getActionClosure(Subject.forUser(ALICE), ProjectResource.of(PROJECT)))
                    .isEmpty();
            assertThat(withExternalRoles.getRoleClosure(Subject.forUser(ALICE), ProjectResource.of(PROJECT)))
                    .isEmpty();
            assertThat(withExternalRoles.hasPermission(Subject.forUser(BOB), ApplicationResource.get(),
                                                       BuiltInAction.EDIT_APPLICATION_SETTINGS)).isFalse();
            assertThat(withExternalRoles.hasPermission(Subject.forAnySignedInUser(), ApplicationResource.get(),
                                                       BuiltInAction.EDIT_APPLICATION_SETTINGS)).isFalse();
            assertThat(withExternalRoles.getAssignedRoles(Subject.forUser(ALICE), ApplicationResource.get()))
                    .isEmpty();
            assertThat(collection.countDocuments()).isZero();
        }
    }
}
