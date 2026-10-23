package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.project.ProjectContext;

import javax.annotation.Nonnull;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * How the ontology editing and search services (docs/01 §5.1, stage S7) reach a project: {@link #open} checks that the
 * project exists, then the caller's permissions, and only then returns its context from the {@link ProjectRegistry},
 * which would create an empty project for any id (07 "设计取舍" of S6). Reads run under the project's read lock
 * (docs/01 §3.4) and changes go through its {@code ChangeManager}, which takes the write lock itself.
 */
public class ProjectContexts {

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final ProjectRegistry projectRegistry;

    public ProjectContexts(@Nonnull AccessManager accessManager,
                           @Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                           @Nonnull ProjectRegistry projectRegistry) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.projectRegistry = checkNotNull(projectRegistry);
    }

    /**
     * The context of the project, loaded if it is not, once the caller has been found to have every action on it.
     *
     * @throws ProjectNotFoundException  if the project does not exist, whoever asks
     * @throws PermissionDeniedException if the caller lacks one of the actions
     */
    @Nonnull
    public ProjectContext open(@Nonnull UserId caller, @Nonnull ProjectId projectId, @Nonnull BuiltInAction... actions) {
        checkNotNull(caller);
        if (projectDetailsRepository.findOne(projectId).isEmpty()) {
            throw new ProjectNotFoundException(projectId);
        }
        var resource = ProjectResource.of(projectId);
        for (var action : actions) {
            accessManager.require(caller, resource, action);
        }
        return projectRegistry.get(projectId);
    }

    /**
     * Opens the project as {@link #open} does and runs the reader under the project's read lock, so that it sees the
     * project between two changes.
     */
    public <T> T read(@Nonnull UserId caller,
                      @Nonnull ProjectId projectId,
                      @Nonnull BuiltInAction action,
                      @Nonnull Function<ProjectContext, T> reader) {
        return read(open(caller, projectId, action), reader);
    }

    /**
     * Runs the reader under the project's read lock. The reader must not apply changes: the lock cannot be upgraded.
     */
    public static <T> T read(@Nonnull ProjectContext context, @Nonnull Function<ProjectContext, T> reader) {
        var lock = context.lock().readLock();
        lock.lock();
        try {
            return reader.apply(context);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Applies the changes as the caller, who must already have been found to have the actions that the change needs.
     * The {@code CREATE_*} permissions of fresh entities are checked while the changes are applied.
     *
     * @return the result, the revision that recorded the changes if any were effective, and the project's event tag
     * after them
     */
    @Nonnull
    public static <R> ChangeOutcome<R> apply(@Nonnull ProjectContext context,
                                             @Nonnull UserId caller,
                                             @Nonnull ChangeListGenerator<R> generator) {
        var result = context.changeManager().applyChanges(caller, generator);
        return new ChangeOutcome<>(result.getSubject(), result.getRevision(), context.events().getCurrentTag());
    }
}
