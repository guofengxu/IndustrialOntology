package org.industrial.ontology.kernel.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The resources a project opens while it is built, released in reverse order of opening, like try-with-resources.
 * Lucene needs that order: the searcher manager closes before the index writer, and the writer before its directory.
 * A failing release is logged and the remaining resources are still released.
 */
final class ProjectResources {

    private static final Logger logger = LoggerFactory.getLogger(ProjectResources.class);

    private final ProjectId projectId;

    private final Deque<Resource> resources = new ArrayDeque<>();

    ProjectResources(@Nonnull ProjectId projectId) {
        this.projectId = checkNotNull(projectId);
    }

    /**
     * Registers a resource that has just been opened and returns it.
     */
    synchronized <T> T add(@Nonnull String name, @Nonnull T resource, @Nonnull Releaser<? super T> releaser) {
        resources.push(new Resource(name, () -> releaser.release(resource)));
        return resource;
    }

    synchronized void releaseAll() {
        while(!resources.isEmpty()) {
            var resource = resources.pop();
            try {
                resource.release().run();
                logger.debug("{} Released {}", projectId, resource.name());
            } catch(Exception e) {
                logger.error("{} Failed to release {}", projectId, resource.name(), e);
            }
        }
    }

    @FunctionalInterface
    interface Releaser<T> {
        void release(T resource) throws Exception;
    }

    @FunctionalInterface
    private interface Release {
        void run() throws Exception;
    }

    private record Resource(String name, Release release) {
    }
}
