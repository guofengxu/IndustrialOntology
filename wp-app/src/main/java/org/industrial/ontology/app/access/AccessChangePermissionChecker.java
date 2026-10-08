package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.api.port.ChangePermissionChecker;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code CREATE_*} checks of the legacy {@code ChangeManager.throwCreatePermissionDeniedIfNecessary}, for one
 * project: the kernel calls back here for each fresh entity of a change list (see {@link ChangePermissionChecker}).
 * The messages are the legacy ones. {@code EDIT_ONTOLOGY} is checked by the service before it applies the changes.
 */
public class AccessChangePermissionChecker implements ChangePermissionChecker {

    private final AccessManager accessManager;

    private final ProjectResource project;

    public AccessChangePermissionChecker(@Nonnull AccessManager accessManager, @Nonnull ProjectId projectId) {
        this.accessManager = checkNotNull(accessManager);
        this.project = ProjectResource.of(projectId);
    }

    @Override
    public void checkCreatePermission(@Nonnull UserId userId, @Nonnull EntityType<?> entityType) {
        if (entityType.equals(EntityType.CLASS)) {
            check(userId, BuiltInAction.CREATE_CLASS, "classes");
        } else if (entityType.equals(EntityType.OBJECT_PROPERTY)
                || entityType.equals(EntityType.DATA_PROPERTY)
                || entityType.equals(EntityType.ANNOTATION_PROPERTY)) {
            check(userId, BuiltInAction.CREATE_PROPERTY, "properties");
        } else if (entityType.equals(EntityType.NAMED_INDIVIDUAL)) {
            check(userId, BuiltInAction.CREATE_INDIVIDUAL, "individuals");
        } else if (entityType.equals(EntityType.DATATYPE)) {
            check(userId, BuiltInAction.CREATE_DATATYPE, "datatypes");
        }
    }

    private void check(UserId userId, BuiltInAction action, String entities) {
        if (!accessManager.hasPermission(Subject.forUser(userId), project, action)) {
            throw new PermissionDeniedException("You do not have permission to create new " + entities);
        }
    }
}
