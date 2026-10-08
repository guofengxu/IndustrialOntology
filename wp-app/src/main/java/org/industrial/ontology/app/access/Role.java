package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.RoleId;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.Role}.
 * <p>
 * A role id with the actions it allows directly and the roles it includes ({@code parents}).
 * <p>
 * {@link #hashCode()} is the legacy one ({@code Objects.hashCode(roleId, parents, actions)}): {@link RoleOracle}
 * collects a closure in a {@code HashSet} as the legacy oracle did, and with the same hash codes it lists the roles
 * in the same order, which is the order the {@code RoleAssignments} documents store them in.
 */
public record Role(@Nonnull RoleId roleId, @Nonnull List<RoleId> parents, @Nonnull List<ActionId> actions) {

    public Role {
        checkNotNull(roleId);
        parents = List.copyOf(checkNotNull(parents));
        actions = List.copyOf(checkNotNull(actions));
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, parents, actions);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Role other
                && roleId.equals(other.roleId)
                && parents.equals(other.parents)
                && actions.equals(other.actions);
    }
}
