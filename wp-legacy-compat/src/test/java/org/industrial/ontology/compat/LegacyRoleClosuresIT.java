package org.industrial.ontology.compat;

import edu.stanford.bmir.protege.web.server.access.RoleOracleImpl;
import edu.stanford.bmir.protege.web.shared.access.ActionId;
import edu.stanford.bmir.protege.web.shared.access.BuiltInRole;
import edu.stanford.bmir.protege.web.shared.access.RoleId;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Pins the role closures that wp-app's {@code RoleOracleTest} compares with (S5, docs/07 5.1-16): the file
 * {@code wp-app/src/test/resources/legacy-access/role-closures.json} must be what the legacy access manager computes
 * for every built-in role and for every ordered pair of them. The closures are stored in {@code RoleAssignments}, in
 * the order of a hash set and with duplicates; matching them exactly keeps documents written by the ported access
 * manager equal to legacy ones.
 * <p>
 * Run with {@code -Dwp.compat.writeMongoSamples=true} to regenerate the file, like the Mongo samples.
 */
public class LegacyRoleClosuresIT {

    private static final Path CLOSURES = Path.of(System.getProperty("wp.compat.roleClosures",
                                                                    "../wp-app/src/test/resources/legacy-access/"
                                                                            + "role-closures.json"));

    @Test
    public void closuresShouldBeWhatTheLegacyAccessManagerComputes() throws IOException {
        var lines = closures();
        assertThat(lines.size(), is(BuiltInRole.values().length * BuiltInRole.values().length));
        if (Boolean.getBoolean("wp.compat.writeMongoSamples")) {
            Files.createDirectories(CLOSURES.getParent());
            Files.writeString(CLOSURES, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
        }
        assertThat(Files.readAllLines(CLOSURES, StandardCharsets.UTF_8), is(lines));
    }

    /**
     * One line for each role alone, then one for each ordered pair of different roles.
     */
    private static List<String> closures() {
        var lines = new ArrayList<String>();
        for (var role : BuiltInRole.values()) {
            lines.add(closure(List.of(role.getRoleId())));
        }
        for (var first : BuiltInRole.values()) {
            for (var second : BuiltInRole.values()) {
                if (first != second) {
                    lines.add(closure(List.of(first.getRoleId(), second.getRoleId())));
                }
            }
        }
        return lines;
    }

    /**
     * As the private {@code getRoleClosure(Collection<RoleId>)} and {@code getActionClosure(Collection<RoleId>)} of
     * the legacy {@code AccessManagerImpl}.
     */
    private static String closure(List<RoleId> roleIds) {
        var roleOracle = RoleOracleImpl.get();
        var roleClosure = roleIds.stream()
                                 .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                                 .map(role -> role.getRoleId().getId())
                                 .collect(toList());
        var actionClosure = roleIds.stream()
                                   .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                                   .flatMap(role -> role.getActions().stream())
                                   .map(ActionId::getId)
                                   .sorted()
                                   .collect(toList());
        return new Document("assignedRoles", roleIds.stream().map(RoleId::getId).collect(toList()))
                .append("roleClosure", roleClosure)
                .append("actionClosure", actionClosure)
                .toJson();
    }
}
