package org.industrial.ontology.app.access;

import org.industrial.ontology.app.access.persistence.RoleAssignmentDocument;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.RoleId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.AccessManagerImpl}: the {@link AccessManager} on the
 * {@code RoleAssignments} collection, plus the roles granted by {@link ExternalRoles}.
 * <p>
 * Differences from the legacy implementation:
 * <ul>
 *     <li>no permission cache. The legacy manager remembered granted permissions in memory and forgot only some of
 *     them when assignments changed; wp-cli now changes assignments from another process ({@code set-permissions},
 *     {@code create-admin}), and a revoked permission has to stop working at once. A check is one query on the
 *     unique {@code (userName, projectId)} index;</li>
 *     <li>setting the assigned roles replaces the document in place and keeps its {@code _id}, where the legacy
 *     manager deleted it and inserted a new one (see {@link RoleAssignmentRepository#setAssignedRoles});</li>
 *     <li>external roles (the Keycloak admin realm role) apply on the application resource.</li>
 * </ul>
 */
public class MongoAccessManager implements AccessManager {

    private final RoleOracle roleOracle;

    private final RoleAssignmentRepository repository;

    private final ExternalRoles externalRoles;

    public MongoAccessManager(@Nonnull RoleOracle roleOracle,
                              @Nonnull RoleAssignmentRepository repository,
                              @Nonnull ExternalRoles externalRoles) {
        this.roleOracle = checkNotNull(roleOracle);
        this.repository = checkNotNull(repository);
        this.externalRoles = checkNotNull(externalRoles);
    }

    @Override
    public void setAssignedRoles(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull Collection<RoleId> roleIds) {
        var assignedRoles = roleIds.stream().map(RoleId::getId).toList();
        repository.setAssignedRoles(toUserName(subject),
                                    toProjectId(resource),
                                    assignedRoles,
                                    roleOracle.getRoleClosureIds(roleIds),
                                    roleOracle.getActionClosureIds(roleIds));
    }

    @Nonnull
    @Override
    public Collection<RoleId> getAssignedRoles(@Nonnull Subject subject, @Nonnull Resource resource) {
        return repository.findAssignments(toUserName(subject), toProjectId(resource))
                         .stream()
                         .flatMap(assignment -> assignment.assignedRoles().stream())
                         .distinct()
                         .map(RoleId::new)
                         .toList();
    }

    @Nonnull
    @Override
    public Collection<RoleId> getRoleClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var stored = applicableAssignments(subject, resource).flatMap(assignment -> assignment.roleClosure().stream());
        var external = roleOracle.getRoleClosureIds(getExternalRoles(subject, resource)).stream();
        return Stream.concat(stored, external).distinct().map(RoleId::new).toList();
    }

    @Nonnull
    @Override
    public Set<ActionId> getActionClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var stored = applicableAssignments(subject, resource).flatMap(assignment -> assignment.actionClosure()
                                                                                              .stream());
        var external = roleOracle.getActionClosureIds(getExternalRoles(subject, resource)).stream();
        return Stream.concat(stored, external).map(ActionId::new).collect(Collectors.toSet());
    }

    @Override
    public boolean hasPermission(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull ActionId actionId) {
        var externalRoleIds = getExternalRoles(subject, resource);
        if (!externalRoleIds.isEmpty() && roleOracle.getActionClosureIds(externalRoleIds).contains(actionId.getId())) {
            return true;
        }
        return repository.hasAction(toUserName(subject), !subject.isGuest(), toProjectId(resource), actionId.getId());
    }

    @Nonnull
    @Override
    public Collection<Subject> getSubjectsWithAccessToResource(@Nonnull Resource resource) {
        return repository.findByProjectId(toProjectId(resource))
                         .stream()
                         .map(assignment -> assignment.userName() == null
                                 ? Subject.forAnySignedInUser()
                                 : Subject.forUser(assignment.userName()))
                         .toList();
    }

    @Nonnull
    @Override
    public Collection<Resource> getResourcesAccessibleToSubject(@Nonnull Subject subject, @Nonnull ActionId actionId) {
        return repository.findByUserNameAndAction(toUserName(subject), actionId.getId())
                         .stream()
                         .map(assignment -> assignment.projectId() == null
                                 ? (Resource) ApplicationResource.get()
                                 : ProjectResource.of(ProjectId.get(assignment.projectId())))
                         .toList();
    }

    @Override
    public void rebuild() {
        for (var assignment : repository.findAll()) {
            var roleIds = assignment.assignedRoles().stream().map(RoleId::new).toList();
            repository.setClosures(checkNotNull(assignment.id()),
                                   roleOracle.getRoleClosureIds(roleIds),
                                   roleOracle.getActionClosureIds(roleIds));
        }
    }

    private Stream<RoleAssignmentDocument> applicableAssignments(Subject subject, Resource resource) {
        return repository.findApplicableAssignments(toUserName(subject), !subject.isGuest(), toProjectId(resource))
                         .stream();
    }

    /**
     * The external roles of a signed-in user on the application; nothing for other subjects and resources.
     */
    private List<RoleId> getExternalRoles(Subject subject, Resource resource) {
        if (!resource.isApplication() || subject.isGuest()) {
            return List.of();
        }
        return subject.getUserId()
                      .map(userId -> List.copyOf(new LinkedHashSet<>(externalRoles.getApplicationRoles(userId))))
                      .orElse(List.of());
    }

    /**
     * The user name of a specific user, or {@code null} for any signed-in user, which is how the collection stores
     * that subject.
     */
    @Nullable
    private static String toUserName(Subject subject) {
        return subject.getUserName().orElse(null);
    }

    /**
     * The project id, or {@code null} for the application, which is how the collection stores that resource.
     */
    @Nullable
    private static String toProjectId(Resource resource) {
        return resource.getProjectId().map(ProjectId::getId).orElse(null);
    }
}
