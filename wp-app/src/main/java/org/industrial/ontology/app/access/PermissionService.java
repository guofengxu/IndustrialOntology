package org.industrial.ontology.app.access;

import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.REBUILD_PERMISSIONS;

/**
 * What a user may do on a project, and the upkeep of the stored role closures (docs/01 §5.1; stage S6): the legacy
 * {@code GetProjectPermissionsActionHandler} and {@code RebuildPermissionsActionHandler}.
 */
public class PermissionService {

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    public PermissionService(@Nonnull AccessManager accessManager,
                             @Nonnull MongoProjectDetailsRepository projectDetailsRepository) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
    }

    /**
     * The actions that the caller may perform on the project, for the client to show or hide what the caller can
     * use. As in the legacy handler every caller may ask about itself; the legacy handler did not check that the
     * project exists and answered with no actions.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller is the guest user
     */
    @Nonnull
    public Set<ActionId> getProjectPermissions(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        accessManager.requireSignedIn(caller);
        if (projectDetailsRepository.findOne(projectId).isEmpty()) {
            throw new ProjectNotFoundException(projectId);
        }
        return accessManager.getActionClosure(Subject.forUser(caller), ProjectResource.of(projectId));
    }

    /**
     * Recomputes the stored role and action closures of every role assignment
     * ({@code POST /api/v1/admin/permissions/rebuild}).
     *
     * @throws PermissionDeniedException if the caller may not rebuild permissions
     */
    public void rebuildPermissions(@Nonnull UserId caller) {
        accessManager.require(caller, ApplicationResource.get(), REBUILD_PERMISSIONS);
        accessManager.rebuild();
    }

    /**
     * Recomputes the stored closures without a permission check, for {@code wp-cli rebuild-permissions}.
     */
    public void rebuildPermissions() {
        accessManager.rebuild();
    }
}
