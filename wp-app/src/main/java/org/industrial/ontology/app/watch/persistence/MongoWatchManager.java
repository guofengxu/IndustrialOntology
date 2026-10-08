package org.industrial.ontology.app.watch.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.watches.Watch;
import org.industrial.ontology.kernel.api.port.WatchManager;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

/**
 * The kernel's {@link WatchManager} port for one project: the queries of the legacy {@code WatchManagerImpl}. Adding
 * and removing watches, and the notifications they trigger, belong to the watch service (S8).
 */
public class MongoWatchManager implements WatchManager {

    private final ProjectId projectId;

    private final WatchRepository repository;

    public MongoWatchManager(@Nonnull ProjectId projectId, @Nonnull WatchRepository repository) {
        this.projectId = checkNotNull(projectId);
        this.repository = checkNotNull(repository);
    }

    @Override
    public Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity) {
        return repository.findWatches(projectId, singleton(watchedEntity))
                         .stream()
                         .map(WatchDocument::toWatch)
                         .collect(toSet());
    }

    @Override
    public Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity, @Nonnull UserId userId) {
        return repository.findWatches(projectId, userId, singleton(watchedEntity))
                         .stream()
                         .map(WatchDocument::toWatch)
                         .collect(toSet());
    }
}
