package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.RoleId;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Converted from the legacy {@code Role_TestCase}; the mocks of the legacy test are plain ids here.
 */
class RoleTest {

    private final RoleId roleId = new RoleId("TheRole");

    private final List<RoleId> parents = List.of(new RoleId("ParentRole"));

    private final List<ActionId> actions = List.of(new ActionId("TheAction"));

    private final Role role = new Role(roleId, parents, actions);

    @Test
    void shouldThrowNullPointerExceptionIfRoleIdIsNull() {
        assertThatThrownBy(() -> new Role(null, parents, actions)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionIfParentsIsNull() {
        assertThatThrownBy(() -> new Role(roleId, null, actions)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerExceptionIfActionsIsNull() {
        assertThatThrownBy(() -> new Role(roleId, parents, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnSuppliedValues() {
        assertThat(role.roleId()).isEqualTo(roleId);
        assertThat(role.parents()).isEqualTo(parents);
        assertThat(role.actions()).isEqualTo(actions);
    }

    @Test
    void shouldBeEqualToOther() {
        assertThat(role).isEqualTo(new Role(roleId, parents, actions)).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToOtherThatHasDifferentValues() {
        assertThat(role).isNotEqualTo(new Role(new RoleId("Other"), parents, actions))
                        .isNotEqualTo(new Role(roleId, List.of(new RoleId("Other")), actions))
                        .isNotEqualTo(new Role(roleId, parents, List.of(new ActionId("Other"))));
    }

    /**
     * The legacy {@code Objects.hashCode(roleId, parents, actions)}, on which the order of stored closures depends
     * (see {@link RoleOracle}).
     */
    @Test
    void shouldHaveTheLegacyHashCode() {
        assertThat(role.hashCode()).isEqualTo(Arrays.hashCode(new Object[]{roleId, parents, actions}))
                                   .isEqualTo(new Role(roleId, parents, actions).hashCode());
    }

    @Test
    void shouldImplementToString() {
        assertThat(role.toString()).startsWith("Role");
    }
}
