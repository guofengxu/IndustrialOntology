package org.industrial.ontology.app.access;

import org.bson.Document;
import org.industrial.ontology.app.persistence.LegacyMongoSamples;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.RoleId;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The built-in role closures. The order checks pin the stored {@code roleClosure} and {@code actionClosure} to the
 * legacy ones: for the S4 samples, and for every built-in role and ordered pair of roles from a file that
 * wp-legacy-compat's {@code LegacyRoleClosuresIT} keeps equal to the legacy access manager's output.
 */
class RoleOracleTest {

    private final RoleOracle oracle = RoleOracle.get();

    @Test
    void shouldIncludeTheRoleAndItsParentsTransitively() {
        var closure = oracle.getRoleClosure(BuiltInRole.CAN_COMMENT.getRoleId())
                            .stream()
                            .map(role -> role.roleId().getId())
                            .toList();

        assertThat(closure).containsExactlyInAnyOrder("CanComment", "CanView", "ProjectViewer", "IssueViewer",
                                                      "ProjectDownloader", "IssueCreator", "IssueCommenter",
                                                      "ObjectCommenter");
    }

    @Test
    void shouldReturnNothingForAnUnknownRole() {
        assertThat(oracle.getRoleClosure(new RoleId("NoSuchRole"))).isEmpty();
        assertThat(oracle.getRoleClosureIds(List.of(new RoleId("NoSuchRole")))).isEmpty();
    }

    @Test
    void systemAdminShouldIncludeTheUserAdministrationActions() {
        var actions = oracle.getActionClosureIds(List.of(BuiltInRole.SYSTEM_ADMIN.getRoleId()));

        assertThat(actions).containsExactly("CreateAccount", "DeleteAnyAccount", "EditApplicationSettings",
                                            "MoveAnyProjectToTrash", "RebuildPermissions", "ResetAnyUserPassword",
                                            "SubstituteUser", "ViewAnyUserDetails")
                           .contains(BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId().getId());
    }

    /**
     * Every role assignment in the legacy sample was written by the legacy code: the closures computed here for its
     * assigned roles are the stored lists, in the same order and with the same duplicates.
     */
    @Test
    void shouldComputeTheClosuresThatTheLegacyCodeStored() {
        var samples = LegacyMongoSamples.documents("RoleAssignments");
        assertThat(samples).hasSize(4);
        for (Document sample : samples) {
            var assignedRoles = sample.getList("assignedRoles", String.class).stream().map(RoleId::new).toList();

            assertThat(oracle.getRoleClosureIds(assignedRoles)).as("roleClosure of %s", assignedRoles)
                                                               .isEqualTo(sample.getList("roleClosure", String.class));
            assertThat(oracle.getActionClosureIds(assignedRoles)).as("actionClosure of %s", assignedRoles)
                                                                 .isEqualTo(sample.getList("actionClosure",
                                                                                          String.class));
        }
    }

    /**
     * {@code legacy-access/role-closures.json} holds what the legacy access manager computes for every built-in role
     * and every ordered pair of them; wp-legacy-compat's {@code LegacyRoleClosuresIT} keeps it equal to the legacy
     * code's output.
     */
    @Test
    void shouldComputeTheLegacyClosuresOfEveryBuiltInRoleAndPair() throws IOException {
        List<String> lines;
        try (var in = getClass().getResourceAsStream("/legacy-access/role-closures.json")) {
            lines = new String(Objects.requireNonNull(in).readAllBytes(), StandardCharsets.UTF_8).lines().toList();
        }
        var count = BuiltInRole.values().length;
        assertThat(lines).hasSize(count * count);
        for (var line : lines) {
            var legacy = Document.parse(line);
            var assignedRoles = legacy.getList("assignedRoles", String.class).stream().map(RoleId::new).toList();

            assertThat(oracle.getRoleClosureIds(assignedRoles)).as("roleClosure of %s", assignedRoles)
                                                               .isEqualTo(legacy.getList("roleClosure", String.class));
            assertThat(oracle.getActionClosureIds(assignedRoles)).as("actionClosure of %s", assignedRoles)
                                                                 .isEqualTo(legacy.getList("actionClosure",
                                                                                          String.class));
        }
    }

    @Test
    void shouldListAnActionOnceForEachRoleThatAllowsIt() {
        var actions = oracle.getActionClosureIds(List.of(BuiltInRole.PROJECT_MANAGER.getRoleId()));

        // LayoutEditor and ProjectViewer both allow AddOrRemoveView.
        assertThat(actions).filteredOn("AddOrRemoveView"::equals).hasSize(2);
        assertThat(actions).isSorted();
    }
}
