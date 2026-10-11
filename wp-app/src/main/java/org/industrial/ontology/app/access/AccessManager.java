package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.AccessManager}, with {@link #require}.
 * <p>
 * Role-based access control: roles are assigned to a {@link Subject} on a {@link Resource}, and a subject may perform
 * an action on a resource if the closure of the roles that apply to it includes the action. The roles that apply to
 * a signed-in user are the user's own and those of "any signed-in user"; the guest user has only its own.
 * <p>
 * Authorization is the service layer's job (docs/01 §5.1, §6): every service method that acts for a caller starts
 * with {@link #require}, and controllers do no checks of their own.
 */
public interface AccessManager {

    /**
     * The roles assigned to exactly this subject on the resource.
     */
    @Nonnull
    Collection<RoleId> getAssignedRoles(@Nonnull Subject subject, @Nonnull Resource resource);

    /**
     * Replaces the roles assigned to the subject on the resource; an empty collection removes them all.
     */
    void setAssignedRoles(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull Collection<RoleId> roleIds);

    /**
     * The roles that apply to the subject on the resource, with the roles they include.
     */
    @Nonnull
    Collection<RoleId> getRoleClosure(@Nonnull Subject subject, @Nonnull Resource resource);

    /**
     * The actions that the subject may perform on the resource.
     */
    @Nonnull
    Set<ActionId> getActionClosure(@Nonnull Subject subject, @Nonnull Resource resource);

    boolean hasPermission(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull ActionId actionId);

    default boolean hasPermission(@Nonnull Subject subject,
                                  @Nonnull Resource resource,
                                  @Nonnull BuiltInAction builtInAction) {
        return hasPermission(subject, resource, builtInAction.getActionId());
    }

    /**
     * The subjects that have role assignments on the resource.
     */
    @Nonnull
    Collection<Subject> getSubjectsWithAccessToResource(@Nonnull Resource resource);

    /**
     * The resources on which exactly this subject has an assignment that allows the action.
     */
    @Nonnull
    Collection<Resource> getResourcesAccessibleToSubject(@Nonnull Subject subject, @Nonnull ActionId actionId);

    /**
     * Recomputes the stored role and action closures of every assignment from its assigned roles, for when the
     * built-in roles have changed (wp-cli {@code rebuild-permissions}).
     */
    void rebuild();

    /**
     * Checks that the subject may perform the action on the resource.
     *
     * @throws PermissionDeniedException if it may not
     */
    default void require(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull BuiltInAction action) {
        if (!hasPermission(subject, resource, action)) {
            throw new PermissionDeniedException(describe(subject) + " does not have the "
                                                        + action.getActionId().getId() + " permission on "
                                                        + describe(resource));
        }
    }

    /**
     * Checks that the user may perform the action on the resource; the usual first line of a service method.
     *
     * @throws PermissionDeniedException if the user may not
     */
    default void require(@Nonnull UserId userId, @Nonnull Resource resource, @Nonnull BuiltInAction action) {
        require(Subject.forUser(userId), resource, action);
    }

    /**
     * Checks that the caller is signed in, for what every signed-in user may do with their own data, such as their
     * API keys. Requests that reach wp-app through the API are always authenticated; this guards the other callers.
     *
     * @throws PermissionDeniedException if the caller is the guest user
     */
    default void requireSignedIn(@Nonnull UserId userId) {
        if (userId.isGuest()) {
            throw new PermissionDeniedException("The guest user must sign in first");
        }
    }

    private static String describe(Subject subject) {
        return subject.getUserName().map(name -> "User " + name).orElse("Any signed-in user");
    }

    private static String describe(Resource resource) {
        return resource.getProjectId().map(projectId -> "project " + projectId.getId()).orElse("the application");
    }
}
