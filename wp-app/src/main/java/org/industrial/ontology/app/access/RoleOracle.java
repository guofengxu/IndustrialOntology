package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.RoleId;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.RoleOracleImpl}, with the closure computations of
 * the legacy {@code AccessManagerImpl}.
 * <p>
 * Knows the built-in roles and computes role closures: a role together with every role it includes, transitively.
 * The {@code RoleAssignments} collection stores the closures of the assigned roles, so that a permission check is a
 * single query; {@link #getRoleClosureIds} and {@link #getActionClosureIds} compute them exactly as the legacy
 * access manager did, including the order and the duplicates, so documents written here equal legacy ones.
 */
public final class RoleOracle {

    private static final RoleOracle BUILT_IN = createBuiltIn();

    private final Map<RoleId, Role> roles;

    private RoleOracle(@Nonnull Map<RoleId, Role> roles) {
        this.roles = roles;
    }

    /**
     * The oracle of the built-in roles ({@link BuiltInRole}).
     */
    @Nonnull
    public static RoleOracle get() {
        return BUILT_IN;
    }

    private static RoleOracle createBuiltIn() {
        var roles = new LinkedHashMap<RoleId, Role>();
        for (var builtInRole : BuiltInRole.values()) {
            var parents = builtInRole.getParents().stream().map(BuiltInRole::getRoleId).toList();
            var actions = builtInRole.getActions().stream().map(BuiltInAction::getActionId).toList();
            roles.put(builtInRole.getRoleId(), new Role(builtInRole.getRoleId(), parents, actions));
        }
        return new RoleOracle(roles);
    }

    /**
     * The role and the roles it includes, transitively; empty for an unknown role. A set, so the iteration order is
     * the hash order that the legacy oracle had (see {@link Role}).
     */
    @Nonnull
    public Collection<Role> getRoleClosure(@Nonnull RoleId roleId) {
        Set<Role> closure = new HashSet<>();
        add(checkNotNull(roleId), closure);
        return closure;
    }

    private void add(RoleId roleId, Set<Role> closure) {
        var role = roles.get(roleId);
        if (role == null) {
            return;
        }
        if (closure.add(role)) {
            role.parents().forEach(parent -> add(parent, closure));
        }
    }

    /**
     * The ids of the closures of the roles, concatenated in the order of {@code roleIds}: the {@code roleClosure} of
     * a role assignment. A role included by two of the roles is listed twice, as it was by the legacy code.
     */
    @Nonnull
    public List<String> getRoleClosureIds(@Nonnull Collection<RoleId> roleIds) {
        return roleIds.stream()
                      .flatMap(roleId -> getRoleClosure(roleId).stream())
                      .map(role -> role.roleId().getId())
                      .toList();
    }

    /**
     * The ids of the actions of the closures of the roles, sorted: the {@code actionClosure} of a role assignment.
     * An action that several roles of the closures allow is listed once for each of them, as it was by the legacy
     * code.
     */
    @Nonnull
    public List<String> getActionClosureIds(@Nonnull Collection<RoleId> roleIds) {
        return roleIds.stream()
                      .flatMap(roleId -> getRoleClosure(roleId).stream())
                      .flatMap(role -> role.actions().stream())
                      .map(ActionId::getId)
                      .sorted()
                      .toList();
    }
}
