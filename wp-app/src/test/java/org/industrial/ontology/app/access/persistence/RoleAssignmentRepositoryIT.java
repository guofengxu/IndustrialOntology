package org.industrial.ontology.app.access.persistence;

import org.bson.Document;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The queries of the legacy access manager. A {@code null} user name is "any signed-in user" and a {@code null}
 * project id the application.
 */
class RoleAssignmentRepositoryIT {

    private static final String PROJECT = "11111111-1111-4111-8111-111111111111";

    private static final String OTHER_PROJECT = "22222222-2222-4222-8222-222222222222";

    private static MongoPersistenceTestContext context;

    private RoleAssignmentRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void assignRoles() {
        context.clear();
        repository = context.bean(RoleAssignmentRepository.class);
        repository.setAssignedRoles("alice", PROJECT, List.of("CanEdit"), List.of("CanEdit", "CanView"),
                                    List.of("EditOntology", "ViewProject"));
        repository.setAssignedRoles(null, PROJECT, List.of("CanView"), List.of("CanView"), List.of("ViewProject"));
        repository.setAssignedRoles("alice", null, List.of("SystemAdmin"), List.of("SystemAdmin"),
                                    List.of("EditApplicationSettings"));
        repository.setAssignedRoles("bob", OTHER_PROJECT, List.of("CanComment"), List.of("CanComment"),
                                    List.of("CreateObjectComment"));
    }

    @Test
    void shouldLeaveTheMissingSubjectOrResourceOutOfTheDocument() {
        var anyUser = context.database().getCollection(RoleAssignmentDocument.COLLECTION)
                             .find(new Document("assignedRoles", "CanView")).first();
        var application = context.database().getCollection(RoleAssignmentDocument.COLLECTION)
                                 .find(new Document("assignedRoles", "SystemAdmin")).first();

        assertThat(anyUser).doesNotContainKey("userName").containsEntry("projectId", PROJECT);
        assertThat(application).doesNotContainKey("projectId").containsEntry("userName", "alice");
    }

    @Test
    void shouldReplaceTheAssignmentOfTheSameSubjectAndResource() {
        var id = repository.findAssignments("alice", PROJECT).get(0).id();

        repository.setAssignedRoles("alice", PROJECT, List.of("CanView"), List.of("CanView"), List.of("ViewProject"));

        assertThat(repository.findAssignments("alice", PROJECT)).singleElement().satisfies(assignment -> {
            assertThat(assignment.id()).isEqualTo(id);
            assertThat(assignment.assignedRoles()).containsExactly("CanView");
        });
        assertThat(repository.findAll()).hasSize(4);
    }

    @Test
    void shouldKeepTheApplicationAndProjectAssignmentsOfAUserApart() {
        assertThat(repository.findAssignments("alice", null)).extracting(RoleAssignmentDocument::assignedRoles)
                                                             .containsExactly(List.of("SystemAdmin"));
        assertThat(repository.findAssignments(null, PROJECT)).extracting(RoleAssignmentDocument::assignedRoles)
                                                             .containsExactly(List.of("CanView"));
    }

    @Test
    void shouldApplyTheAssignmentsOfAnySignedInUserUnlessAskedNotTo() {
        assertThat(repository.findApplicableAssignments("alice", true, PROJECT))
                .extracting(RoleAssignmentDocument::assignedRoles)
                .containsExactlyInAnyOrder(List.of("CanEdit"), List.of("CanView"));
        assertThat(repository.findApplicableAssignments("carol", true, PROJECT))
                .extracting(RoleAssignmentDocument::assignedRoles)
                .containsExactly(List.of("CanView"));
        assertThat(repository.findApplicableAssignments("carol", false, PROJECT)).isEmpty();
    }

    @Test
    void shouldCheckActionsAgainstTheClosures() {
        assertThat(repository.hasAction("alice", true, PROJECT, "EditOntology")).isTrue();
        assertThat(repository.hasAction("carol", true, PROJECT, "ViewProject")).isTrue();
        assertThat(repository.hasAction("carol", true, PROJECT, "EditOntology")).isFalse();
        assertThat(repository.hasAction("alice", true, OTHER_PROJECT, "CreateObjectComment")).isFalse();
        assertThat(repository.hasAction("alice", false, null, "EditApplicationSettings")).isTrue();
    }

    @Test
    void shouldFindTheSubjectsOfAResourceAndTheResourcesOfASubject() {
        assertThat(repository.findByProjectId(PROJECT)).extracting(RoleAssignmentDocument::userName)
                                                       .containsExactlyInAnyOrder("alice", null);
        assertThat(repository.findByUserNameAndAction("alice", "EditApplicationSettings"))
                .extracting(RoleAssignmentDocument::projectId)
                .containsExactly((String) null);
    }

    @Test
    void shouldStoreRecomputedClosures() {
        var assignment = repository.findAssignments("bob", OTHER_PROJECT).get(0);

        repository.setClosures(assignment.id(), List.of("CanComment", "CanView"), List.of("ViewProject"));

        assertThat(repository.hasAction("bob", false, OTHER_PROJECT, "ViewProject")).isTrue();
        assertThat(repository.hasAction("bob", false, OTHER_PROJECT, "CreateObjectComment")).isFalse();
    }
}
