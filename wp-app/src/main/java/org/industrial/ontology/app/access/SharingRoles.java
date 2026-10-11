package org.industrial.ontology.app.access;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.sharing.SharingPermission;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

import static org.industrial.ontology.domain.core.BuiltInRole.CAN_COMMENT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_MANAGE;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.sharing.Roles}: the sharing permissions that the sharing
 * settings show are the roles {@code CanView}, {@code CanComment}, {@code CanEdit} and {@code CanManage}.
 */
public final class SharingRoles {

    private SharingRoles() {
    }

    /**
     * The strongest sharing permission among the roles, if they include one of the four sharing roles.
     */
    @Nonnull
    public static Optional<SharingPermission> toSharingPermission(@Nonnull Collection<RoleId> roles) {
        if (roles.contains(CAN_MANAGE.getRoleId())) {
            return Optional.of(SharingPermission.MANAGE);
        }
        else if (roles.contains(CAN_EDIT.getRoleId())) {
            return Optional.of(SharingPermission.EDIT);
        }
        else if (roles.contains(CAN_COMMENT.getRoleId())) {
            return Optional.of(SharingPermission.COMMENT);
        }
        else if (roles.contains(CAN_VIEW.getRoleId())) {
            return Optional.of(SharingPermission.VIEW);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * The one role that grants the sharing permission.
     */
    @Nonnull
    public static ImmutableSet<RoleId> fromSharingPermission(@Nonnull SharingPermission sharingPermission) {
        return switch (sharingPermission) {
            case MANAGE -> ImmutableSet.of(CAN_MANAGE.getRoleId());
            case EDIT -> ImmutableSet.of(CAN_EDIT.getRoleId());
            case COMMENT -> ImmutableSet.of(CAN_COMMENT.getRoleId());
            case VIEW -> ImmutableSet.of(CAN_VIEW.getRoleId());
        };
    }
}
