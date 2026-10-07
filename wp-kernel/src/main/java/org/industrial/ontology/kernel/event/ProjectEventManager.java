package org.industrial.ontology.kernel.event;

import org.industrial.ontology.domain.core.HasDispose;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.event.EventList;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.EventManager}.
 * <p>
 * Keeps a project's recently posted events in tagged buckets so that clients can catch up from the last
 * {@link EventTag} they saw, and forwards each posted batch to listeners (wp-app's {@code ProjectEventBroadcaster}
 * feeds SSE from here, docs/01 §5.2). Differences from the legacy class:
 * <ul>
 *     <li>events are the sealed {@link ProjectEvent} records and the GWT event bus is replaced by listeners;</li>
 *     <li>retention and batch limit are configurable ({@code webprotege.events.retention}, default 10 minutes
 *     instead of the legacy 60 seconds, and {@code webprotege.events.max-batch}, docs/00 §8);</li>
 *     <li>expired buckets are purged on a scheduler shared by all projects (wp-app's {@code KernelExecutors});
 *     {@link #dispose()} cancels this project's task but never shuts the scheduler down (docs/01 §3.4).</li>
 * </ul>
 */
public class ProjectEventManager implements HasDispose, HasPostEvents<ProjectEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectEventManager.class);

    public static final Duration DEFAULT_RETENTION = Duration.ofMinutes(10);

    public static final int DEFAULT_MAX_BATCH_SIZE = 200;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Lock readLock = lock.readLock();

    private final Lock writeLock = lock.writeLock();

    private final Deque<EventBucket> eventQueue = new ArrayDeque<>();

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final Duration retention;

    private final int maxBatchSize;

    @Nonnull
    private final LongSupplier clock;

    private final List<Consumer<List<ProjectEvent>>> listeners = new CopyOnWriteArrayList<>();

    @Nonnull
    private final ScheduledFuture<?> purgeTask;

    private EventTag currentTag = EventTag.getFirst();

    /** The newest tag whose bucket has been purged; events after it are still available. */
    private EventTag lastPurgedTag = EventTag.getFirst();

    public ProjectEventManager(@Nonnull ProjectId projectId,
                               @Nonnull Duration retention,
                               int maxBatchSize,
                               @Nonnull ScheduledExecutorService purgeScheduler) {
        this(projectId, retention, maxBatchSize, purgeScheduler, System::currentTimeMillis);
    }

    ProjectEventManager(@Nonnull ProjectId projectId,
                        @Nonnull Duration retention,
                        int maxBatchSize,
                        @Nonnull ScheduledExecutorService purgeScheduler,
                        @Nonnull LongSupplier clock) {
        this.projectId = checkNotNull(projectId);
        this.retention = checkNotNull(retention);
        checkArgument(!retention.isNegative() && !retention.isZero(), "retention must be positive");
        checkArgument(maxBatchSize > 0, "maxBatchSize must be positive");
        this.maxBatchSize = maxBatchSize;
        this.clock = checkNotNull(clock);
        long period = retention.toMillis();
        this.purgeTask = checkNotNull(purgeScheduler).scheduleAtFixedRate(this::removeExpiredEvents, period, period,
                                                                          TimeUnit.MILLISECONDS);
    }

    @Override
    public EventTag postEvent(@Nonnull ProjectEvent event) {
        return postEvents(List.of(checkNotNull(event, "event must not be null")));
    }

    /**
     * Posts a batch of events as one bucket.
     * <p>
     * Like the legacy implementation, batches larger than the configured limit are not recorded (clients
     * refresh fully instead) and the current tag is returned unchanged.
     */
    @Override
    public EventTag postEvents(@Nonnull List<ProjectEvent> events) {
        checkNotNull(events, "events must not be null");
        if (events.size() > maxBatchSize) {
            logger.debug("{} Not recording a batch of {} events (limit {})", projectId, events.size(), maxBatchSize);
            return getCurrentTag();
        }
        final EventTag tag;
        writeLock.lock();
        try {
            currentTag = currentTag.next();
            tag = currentTag;
            eventQueue.add(new EventBucket(clock.getAsLong(), List.copyOf(events), tag));
        } finally {
            writeLock.unlock();
        }
        var distinct = List.copyOf(new LinkedHashSet<>(events));
        for (var listener : listeners) {
            try {
                listener.accept(distinct);
            } catch (RuntimeException e) {
                logger.error("{} Project event listener failed", projectId, e);
            }
        }
        return tag;
    }

    /** All retained events posted at or after {@code fromTag}, without duplicates. */
    public EventList<ProjectEvent> getEventsFromTag(@Nonnull EventTag fromTag) {
        checkNotNull(fromTag, "tag must not be null");
        List<ProjectEvent> resultList = new ArrayList<>();
        final EventTag curTag;
        readLock.lock();
        try {
            curTag = currentTag;
            for (EventBucket bucket : eventQueue) {
                if (bucket.tag().isGreaterOrEqualTo(fromTag)) {
                    resultList.addAll(bucket.events());
                }
            }
        } finally {
            readLock.unlock();
        }
        final EventTag toTag = curTag.next();
        if (resultList.isEmpty()) {
            return new EventList<>(fromTag, toTag);
        }
        return new EventList<>(fromTag, new LinkedHashSet<>(resultList), toTag);
    }

    /**
     * Whether every event posted at or after {@code fromTag} is still retained. When this is {@code false} a
     * client resuming from {@code fromTag} has missed events and must reload (HTTP 410 in docs/02 §5).
     */
    public boolean isRetainedFrom(@Nonnull EventTag fromTag) {
        checkNotNull(fromTag);
        readLock.lock();
        try {
            return fromTag.compareTo(lastPurgedTag) > 0 || lastPurgedTag.equals(EventTag.getFirst());
        } finally {
            readLock.unlock();
        }
    }

    public EventTag getCurrentTag() {
        readLock.lock();
        try {
            return currentTag;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Registers a listener for every posted batch. Listeners run on the posting thread, after the project write
     * lock has been released, and must not block.
     *
     * @return a handle that unregisters the listener
     */
    public AutoCloseable addListener(@Nonnull Consumer<List<ProjectEvent>> listener) {
        listeners.add(checkNotNull(listener));
        return () -> listeners.remove(listener);
    }

    void removeExpiredEvents() {
        long now = clock.getAsLong();
        writeLock.lock();
        try {
            while (!eventQueue.isEmpty()) {
                EventBucket bucket = eventQueue.peek();
                if (now - bucket.timestamp() > retention.toMillis()) {
                    eventQueue.poll();
                    lastPurgedTag = bucket.tag();
                } else {
                    break;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void dispose() {
        purgeTask.cancel(false);
        listeners.clear();
    }

    private record EventBucket(long timestamp, List<ProjectEvent> events, EventTag tag) {
    }
}
