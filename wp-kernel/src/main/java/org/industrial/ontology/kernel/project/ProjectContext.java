package org.industrial.ontology.kernel.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.change.ChangeManager;
import org.industrial.ontology.kernel.crud.ProjectEntityCrudKitHandlerCache;
import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.hierarchy.HierarchyProviders;
import org.industrial.ontology.kernel.index.IndexUpdater;
import org.industrial.ontology.kernel.index.ProjectIndexes;
import org.industrial.ontology.kernel.lucene.LuceneIndexWriter;
import org.industrial.ontology.kernel.mansyntax.render.DeprecatedEntityChecker;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.kernel.match.MatchingEngine;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.kernel.revision.RevisionManager;
import org.industrial.ontology.kernel.revision.RevisionStore;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.kernel.shortform.LanguageManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The object graph of one loaded project (docs/01 §2), replacing the legacy Dagger {@code ProjectComponent}.
 * {@link ProjectContextFactory} builds it; wp-app's {@code ProjectRegistry} caches it and closes it when the project
 * goes dormant.
 * <p>
 * Reads take {@link #lock()}'s read lock; {@link ChangeManager#applyChanges} takes the write lock itself, after its
 * change-processing lock (docs/01 §3.4).
 * <p>
 * {@link #close()} waits for running reads and writes, then stops the project's event purge task, flushes pending
 * revisions and closes its Lucene searcher, writer and directory. The shared {@link KernelThreadPools} stay
 * running.
 */
public final class ProjectContext implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ProjectContext.class);

    private final ProjectId projectId;

    private final OWLDataFactory dataFactory;

    private final ProjectIndexes indexes;

    private final IndexUpdater indexUpdater;

    private final RevisionStore revisionStore;

    private final RevisionManager revisionManager;

    private final HierarchyProviders hierarchies;

    private final LanguageManager languageManager;

    private final DictionaryManager dictionary;

    private final LuceneIndexWriter searchIndex;

    private final DeprecatedEntityChecker deprecatedEntityChecker;

    private final RenderingManager rendering;

    private final EntityNodeRenderer entityNodeRenderer;

    private final MatchingEngine matchingEngine;

    private final MatcherFactory matcherFactory;

    private final DefaultOntologyIdManager defaultOntologyIdManager;

    private final ProjectEntityCrudKitHandlerCache crudKits;

    private final ProjectEventManager events;

    private final ChangeManager changeManager;

    private final ReadWriteLock lock;

    private final ProjectResources resources;

    private final AtomicBoolean closed = new AtomicBoolean();

    private volatile Instant lastAccess;

    ProjectContext(@Nonnull ProjectId projectId,
                   @Nonnull OWLDataFactory dataFactory,
                   @Nonnull ProjectIndexes indexes,
                   @Nonnull IndexUpdater indexUpdater,
                   @Nonnull RevisionStore revisionStore,
                   @Nonnull RevisionManager revisionManager,
                   @Nonnull HierarchyProviders hierarchies,
                   @Nonnull LanguageManager languageManager,
                   @Nonnull DictionaryManager dictionary,
                   @Nonnull LuceneIndexWriter searchIndex,
                   @Nonnull DeprecatedEntityChecker deprecatedEntityChecker,
                   @Nonnull RenderingManager rendering,
                   @Nonnull EntityNodeRenderer entityNodeRenderer,
                   @Nonnull MatchingEngine matchingEngine,
                   @Nonnull MatcherFactory matcherFactory,
                   @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                   @Nonnull ProjectEntityCrudKitHandlerCache crudKits,
                   @Nonnull ProjectEventManager events,
                   @Nonnull ChangeManager changeManager,
                   @Nonnull ReadWriteLock lock,
                   @Nonnull ProjectResources resources,
                   @Nonnull Instant loadedAt) {
        this.projectId = checkNotNull(projectId);
        this.dataFactory = checkNotNull(dataFactory);
        this.indexes = checkNotNull(indexes);
        this.indexUpdater = checkNotNull(indexUpdater);
        this.revisionStore = checkNotNull(revisionStore);
        this.revisionManager = checkNotNull(revisionManager);
        this.hierarchies = checkNotNull(hierarchies);
        this.languageManager = checkNotNull(languageManager);
        this.dictionary = checkNotNull(dictionary);
        this.searchIndex = checkNotNull(searchIndex);
        this.deprecatedEntityChecker = checkNotNull(deprecatedEntityChecker);
        this.rendering = checkNotNull(rendering);
        this.entityNodeRenderer = checkNotNull(entityNodeRenderer);
        this.matchingEngine = checkNotNull(matchingEngine);
        this.matcherFactory = checkNotNull(matcherFactory);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
        this.crudKits = checkNotNull(crudKits);
        this.events = checkNotNull(events);
        this.changeManager = checkNotNull(changeManager);
        this.lock = checkNotNull(lock);
        this.resources = checkNotNull(resources);
        this.lastAccess = checkNotNull(loadedAt);
    }

    @Nonnull
    public ProjectId projectId() {
        return projectId;
    }

    /**
     * The project's data factory. OWL objects passed to this project's components should come from it.
     */
    @Nonnull
    public OWLDataFactory dataFactory() {
        return dataFactory;
    }

    @Nonnull
    public ProjectIndexes indexes() {
        return indexes;
    }

    @Nonnull
    public IndexUpdater indexUpdater() {
        return indexUpdater;
    }

    @Nonnull
    public RevisionStore revisionStore() {
        return revisionStore;
    }

    @Nonnull
    public RevisionManager revisionManager() {
        return revisionManager;
    }

    @Nonnull
    public HierarchyProviders hierarchies() {
        return hierarchies;
    }

    @Nonnull
    public LanguageManager languageManager() {
        return languageManager;
    }

    /**
     * The Lucene-backed short-form dictionary and entity search.
     */
    @Nonnull
    public DictionaryManager dictionary() {
        return dictionary;
    }

    /**
     * Writes the project's Lucene index; {@link LuceneIndexWriter#rebuildIndex()} re-creates it from the indexes.
     */
    @Nonnull
    public LuceneIndexWriter searchIndex() {
        return searchIndex;
    }

    @Nonnull
    public DeprecatedEntityChecker deprecatedEntityChecker() {
        return deprecatedEntityChecker;
    }

    @Nonnull
    public RenderingManager rendering() {
        return rendering;
    }

    @Nonnull
    public EntityNodeRenderer entityNodeRenderer() {
        return entityNodeRenderer;
    }

    @Nonnull
    public MatchingEngine matchingEngine() {
        return matchingEngine;
    }

    /**
     * Creates matchers for entity criteria; it is also the project's relationship, hierarchy-position and entity
     * matcher factory.
     */
    @Nonnull
    public MatcherFactory matcherFactory() {
        return matcherFactory;
    }

    @Nonnull
    public DefaultOntologyIdManager defaultOntologyIdManager() {
        return defaultOntologyIdManager;
    }

    @Nonnull
    public ProjectEntityCrudKitHandlerCache crudKits() {
        return crudKits;
    }

    /**
     * The project's event buckets, read with {@code getEventsFromTag(EventTag)}.
     */
    @Nonnull
    public ProjectEventManager events() {
        return events;
    }

    @Nonnull
    public ChangeManager changeManager() {
        return changeManager;
    }

    /**
     * The project-wide read/write lock, also held by {@link ChangeManager}.
     */
    @Nonnull
    public ReadWriteLock lock() {
        return lock;
    }

    @Nonnull
    public Instant lastAccess() {
        return lastAccess;
    }

    /**
     * Records that the project was used at {@code now}.
     */
    public void touch(@Nonnull Instant now) {
        lastAccess = checkNotNull(now);
    }

    public boolean isClosed() {
        return closed.get();
    }

    /**
     * Releases the project's resources once; later calls do nothing. Waits for reads and writes that hold the project
     * lock, so it must not be called while the calling thread holds the read lock.
     */
    @Override
    public void close() {
        if(!closed.compareAndSet(false, true)) {
            return;
        }
        logger.info("{} Closing project", projectId);
        lock.writeLock().lock();
        try {
            resources.releaseAll();
        } finally {
            lock.writeLock().unlock();
        }
        logger.info("{} Closed project", projectId);
    }

    @Override
    public String toString() {
        return "ProjectContext{" + projectId + (closed.get() ? ", closed" : "") + "}";
    }
}
