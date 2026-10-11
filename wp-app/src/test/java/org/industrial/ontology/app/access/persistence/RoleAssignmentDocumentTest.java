package org.industrial.ontology.app.access.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Converted from the legacy {@code RoleAssignment_TestCase}. One difference is intended: the legacy class threw a
 * {@code NullPointerException} for a missing role list, while this record, which also reads stored documents, takes
 * a missing list as empty, as Morphia did when it loaded a document without the field.
 */
class RoleAssignmentDocumentTest {

    private static final String USER_NAME = "The userName";

    private static final String PROJECT_ID = "The projectId";

    private final List<String> assignedRoles = List.of("AssignedRole");

    private final List<String> roleClosure = List.of("AssignedRole", "ParentRole");

    private final List<String> actionClosure = List.of("ActionA", "ActionB");

    private final RoleAssignmentDocument assignment = assignment(USER_NAME, PROJECT_ID);

    private RoleAssignmentDocument assignment(String userName, String projectId) {
        return new RoleAssignmentDocument(null, userName, projectId, assignedRoles, roleClosure, actionClosure);
    }

    @Test
    void shouldAcceptANullUserNameOrProjectId() {
        assertThat(assignment(null, PROJECT_ID).userName()).isNull();
        assertThat(assignment(USER_NAME, null).projectId()).isNull();
    }

    @Test
    void shouldReturnSuppliedValues() {
        assertThat(assignment.userName()).isEqualTo(USER_NAME);
        assertThat(assignment.projectId()).isEqualTo(PROJECT_ID);
        assertThat(assignment.assignedRoles()).isEqualTo(assignedRoles);
        assertThat(assignment.roleClosure()).isEqualTo(roleClosure);
        assertThat(assignment.actionClosure()).isEqualTo(actionClosure);
    }

    @Test
    void shouldTakeAMissingListAsEmpty() {
        var withoutLists = new RoleAssignmentDocument(null, USER_NAME, PROJECT_ID, null, null, null);

        assertThat(withoutLists.assignedRoles()).isEmpty();
        assertThat(withoutLists.roleClosure()).isEmpty();
        assertThat(withoutLists.actionClosure()).isEmpty();
    }

    @Test
    void shouldBeEqualToOther() {
        assertThat(assignment).isEqualTo(assignment(USER_NAME, PROJECT_ID))
                              .hasSameHashCodeAs(assignment(USER_NAME, PROJECT_ID))
                              .isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToOtherThatHasDifferentValues() {
        assertThat(assignment)
                .isNotEqualTo(assignment("Other user", PROJECT_ID))
                .isNotEqualTo(assignment(USER_NAME, "Other project"))
                .isNotEqualTo(new RoleAssignmentDocument(null, USER_NAME, PROJECT_ID, List.of("OtherAssignedRole"),
                                                         roleClosure, actionClosure))
                .isNotEqualTo(new RoleAssignmentDocument(null, USER_NAME, PROJECT_ID, assignedRoles,
                                                         List.of("OtherRoleClosure"), actionClosure))
                .isNotEqualTo(new RoleAssignmentDocument(null, USER_NAME, PROJECT_ID, assignedRoles, roleClosure,
                                                         List.of("OtherActionClosure")));
    }

    @Test
    void shouldImplementToString() {
        assertThat(assignment.toString()).startsWith("RoleAssignment");
    }
}
