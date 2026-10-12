package org.industrial.ontology.app.event;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.kernel.event.EventBatch;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * Pushes each project's events to its open event streams (docs/01 §5.2, stage S8), in place of the legacy clients'
 * polling of {@code GetProjectEvents}.
 * <p>
 * A subscription listens to its project's {@code ProjectEventManager}, which hands it each batch on the posting thread
 * (for a change, inside the change lock). The subscription only queues the batch there; a sender thread takes the
 * queue and writes to the client, so a slow client never holds up a change. Sinks are only ever called on sender
 * threads, since a sink may block on a client that does not read. When more than
 * {@code webprotege.events.max-queued-batches} wait, the subscription is closed and the client reconnects from the last
 * batch it received.
 * <p>
 * Catching up neither misses nor repeats a batch: the subscription registers its listener first, holds back the
 * batches the listener receives, reads the retained batches after the client's tag, then queues both in tag order,
 * skipping a batch whose tag it has already queued.
 * <p>
 * Every heartbeat ({@code webprotege.events.heartbeat}) each subscription is pinged, keeps its project loaded and checks
 * again that its user may view the project and that its credentials are still good; a batch is also preceded by those
 * checks when the last ones are older than {@link #PERMISSION_CHECK_INTERVAL}. Project permissions do not depend on the
 * request thread (only application roles from the identity provider do, 07 S5), so the check gives the same answer
 * here. A subscription whose project has been closed is closed too: the project's tags restart when it is loaded again.
 */
public class ProjectEventBroadcaster implements AutoCloseable {

    /**
     * The user has too many open event streams (429).
     */
    public static final String TOO_MANY_SUBSCRIPTIONS = "TOO_MANY_SUBSCRIPTIONS";

    /**
     * How old the last permission and credentials check may be when a batch is sent.
     */
    public static final Duration PERMISSION_CHECK_INTERVAL = Duration.ofSeconds(5);

    private static final Logger logger = LoggerFactory.getLogger(ProjectEventBroadcaster.class);

    private static final Object HEARTBEAT = new Object();

    private final ProjectRegistry registry;

    private final AccessManager accessManager;

    private final int maxSubscriptionsPerUser;

    private final int maxQueuedBatches;

    private final ScheduledExecutorService heartbeats;

    /**
     * Unbounded: a sender blocks while its client is slow, and only one sender works on a subscription at a time, so
     * there are at most as many busy senders as open subscriptions, which the per-user limit bounds.
     */
    private final ExecutorService senders;

    private final Set<Subscription> subscriptions = ConcurrentHashMap.newKeySet();

    private final ConcurrentMap<UserId, Integer> subscriptionsPerUser = new ConcurrentHashMap<>();

    private volatile boolean closed;

    private volatile Consumer<CatchUpStep> catchUpProbe = step -> {
    };

    /**
     * @param heartbeat              how often subscriptions are pinged and checked
     * @param maxSubscriptionsPerUser how many subscriptions one user may have open at a time
     * @param maxQueuedBatches        how many batches may wait for a client before its subscription is closed, and how
     *                                many a client may catch up with
     */
    public ProjectEventBroadcaster(@Nonnull ProjectRegistry registry,
                                   @Nonnull AccessManager accessManager,
                                   @Nonnull Duration heartbeat,
                                   int maxSubscriptionsPerUser,
                                   int maxQueuedBatches) {
        this.registry = checkNotNull(registry);
        this.accessManager = checkNotNull(accessManager);
        checkArgument(heartbeat.compareTo(Duration.ZERO) > 0, "heartbeat must be positive: %s", heartbeat);
        checkArgument(maxSubscriptionsPerUser > 0, "maxSubscriptionsPerUser must be positive");
        checkArgument(maxQueuedBatches > 0, "maxQueuedBatches must be positive");
        this.maxSubscriptionsPerUser = maxSubscriptionsPerUser;
        this.maxQueuedBatches = maxQueuedBatches;
        this.heartbeats = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("project-events-heartbeat-%d").setDaemon(true).build());
        this.senders = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("project-events-sender-%d").setDaemon(true).build());
        var period = heartbeat.toMillis();
        heartbeats.scheduleAtFixedRate(this::heartbeat, period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends the project's batches after {@code since} to the sink, then every batch as it is posted. The caller must
     * already have been found to have {@code ViewProject} on the project.
     *
     * @param since the client's position, or {@code null} for only the batches posted from now on
     * @throws WpException {@value ProjectEventService#EVENTS_EXPIRED} (410) if the batches after {@code since} are not
     *                     available; {@value #TOO_MANY_SUBSCRIPTIONS} (429) if the user has too many open
     *                     subscriptions
     */
    @Nonnull
    public Subscription subscribe(@Nonnull ProjectContext context,
                                  @Nonnull UserId userId,
                                  @Nullable EventPosition since,
                                  @Nonnull ProjectEventSink sink) {
        checkNotNull(context);
        requireOpen();
        acquire(userId);
        var subscription = new Subscription(context, userId, sink);
        try {
            var events = context.events();
            subscription.listener = events.addListener(subscription::onLive);
            catchUpProbe.accept(CatchUpStep.LISTENING);
            // Read after the listener is registered: every later batch reaches the listener
            var current = events.getCurrentTag();
            var from = since != null ? since : EventPosition.of(current);
            var backlog = ProjectEventService.backlog(events, context.projectId(), from, current, maxQueuedBatches);
            catchUpProbe.accept(CatchUpStep.BACKLOG_READ);
            subscriptions.add(subscription);
            // close() may have run since the first check without seeing this subscription
            requireOpen();
            subscription.catchUp(from.tag(), backlog);
            return subscription;
        } catch (RuntimeException e) {
            subscription.end(false);
            throw e;
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("The project event broadcaster has been closed");
        }
    }

    /**
     * Lets tests post batches at the two moments of {@link #subscribe} where a batch posted by another thread must be
     * neither lost nor sent twice: once the listener is registered (the batch is both held back and read back), and
     * once the backlog has been read (the batch is only held back). Package-private, for tests only.
     */
    void setCatchUpProbe(@Nonnull Consumer<CatchUpStep> probe) {
        catchUpProbe = checkNotNull(probe);
    }

    enum CatchUpStep {
        LISTENING,
        BACKLOG_READ
    }

    /**
     * The number of open subscriptions (the {@code wp.events.sse.connections} gauge of 07 7-2).
     */
    public int subscriptionCount() {
        return subscriptions.size();
    }

    /**
     * How many batches may wait for a client, and how many a client may catch up with.
     */
    int maxQueuedBatches() {
        return maxQueuedBatches;
    }

    private void acquire(UserId userId) {
        var refused = new AtomicBoolean();
        subscriptionsPerUser.compute(userId, (id, count) -> {
            var open = count == null ? 0 : count;
            if (open >= maxSubscriptionsPerUser) {
                refused.set(true);
                return count;
            }
            return open + 1;
        });
        if (refused.get()) {
            throw new WpException(TOO_MANY_SUBSCRIPTIONS, 429, "User " + userId.getUserName() + " already has "
                    + maxSubscriptionsPerUser + " open event streams");
        }
    }

    private void release(UserId userId) {
        subscriptionsPerUser.computeIfPresent(userId, (id, count) -> count <= 1 ? null : count - 1);
    }

    private void heartbeat() {
        for (var subscription : subscriptions) {
            try {
                if (registry.touchIfOpen(subscription.context)) {
                    subscription.enqueueHeartbeat();
                } else {
                    subscription.end(true);
                }
            } catch (RuntimeException e) {
                logger.warn("{} Heartbeat of an event stream of {} failed", subscription.projectId(),
                            subscription.userId, e);
            }
        }
    }

    /**
     * Closes every subscription, ending its stream, and stops the threads. Streams whose clients do not read are
     * given five seconds.
     */
    @Override
    public void close() {
        closed = true;
        heartbeats.shutdownNow();
        for (var subscription : List.copyOf(subscriptions)) {
            subscription.end(true);
        }
        senders.shutdown();
        try {
            if (!senders.awaitTermination(5, TimeUnit.SECONDS)) {
                senders.shutdownNow();
            }
        } catch (InterruptedException e) {
            senders.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * An open event stream of one user on one project.
     */
    public final class Subscription {

        private static final int NOTHING = 0;

        private static final int START_SENDER = 1;

        private static final int OVERFLOW = 2;

        private final ProjectContext context;

        private final UserId userId;

        private final ProjectEventSink sink;

        private final AtomicBoolean ended = new AtomicBoolean();

        /** Batches and heartbeats waiting to be sent. Guarded by this. */
        private final ArrayDeque<Object> queue = new ArrayDeque<>();

        /** Batches received while catching up. Guarded by this. */
        private final List<EventBatch> heldBack = new ArrayList<>();

        /** Guarded by this. */
        private boolean catchingUp = true;

        /** Whether a sender is taking the queue. Guarded by this. */
        private boolean sending;

        /** The tag of the last batch queued. Guarded by this. */
        private EventTag lastQueued;

        private volatile AutoCloseable listener;

        /** {@link System#nanoTime()} of the last permission check; the subscriber has just been checked. */
        private volatile long lastPermissionCheck = System.nanoTime();

        private Subscription(ProjectContext context, UserId userId, ProjectEventSink sink) {
            this.context = checkNotNull(context);
            this.userId = checkNotNull(userId);
            this.sink = checkNotNull(sink);
        }

        @Nonnull
        public ProjectId projectId() {
            return context.projectId();
        }

        @Nonnull
        public UserId userId() {
            return userId;
        }

        public boolean isOpen() {
            return !ended.get();
        }

        /**
         * Ends the subscription because its client has gone; the sink is not closed.
         */
        public void close() {
            end(false);
        }

        /** On the posting thread: only queue. */
        private void onLive(EventBatch batch) {
            int result;
            synchronized (this) {
                if (catchingUp) {
                    heldBack.add(batch);
                    return;
                }
                result = offer(batch, true);
            }
            afterOffer(result);
        }

        private void catchUp(EventTag since, List<EventBatch> backlog) {
            int result = NOTHING;
            synchronized (this) {
                lastQueued = since;
                // The backlog has been limited already; the queue limit is for live batches
                for (var batch : backlog) {
                    result = Math.max(result, offer(batch, false));
                }
                for (var batch : heldBack) {
                    result = Math.max(result, offer(batch, true));
                }
                heldBack.clear();
                catchingUp = false;
            }
            afterOffer(result);
        }

        private void enqueueHeartbeat() {
            int result;
            synchronized (this) {
                result = enqueue(HEARTBEAT);
            }
            afterOffer(result);
        }

        /** Guarded by this. */
        private int offer(EventBatch batch, boolean live) {
            if (batch.tag().compareTo(lastQueued) <= 0) {
                // Read back while catching up and also received by the listener
                return NOTHING;
            }
            if (live && queue.size() >= maxQueuedBatches) {
                return OVERFLOW;
            }
            lastQueued = batch.tag();
            return enqueue(batch);
        }

        /** Guarded by this. */
        private int enqueue(Object item) {
            if (ended.get()) {
                return NOTHING;
            }
            if (item == HEARTBEAT && queue.size() >= maxQueuedBatches) {
                // The client has not taken anything for a whole heartbeat and more
                return OVERFLOW;
            }
            queue.add(item);
            if (sending) {
                return NOTHING;
            }
            sending = true;
            return START_SENDER;
        }

        private void afterOffer(int result) {
            if (result == OVERFLOW) {
                logger.info("{} Closing the event stream of {}: it fell more than {} batches behind", projectId(),
                            userId, maxQueuedBatches);
                end(true);
            } else if (result == START_SENDER) {
                try {
                    senders.execute(this::send);
                } catch (RejectedExecutionException e) {
                    // Shutting down
                    end(true);
                }
            }
        }

        /** On a sender thread. */
        private void send() {
            var epoch = context.events().epoch();
            while (true) {
                Object item;
                synchronized (this) {
                    item = queue.poll();
                    if (item == null || ended.get()) {
                        sending = false;
                        return;
                    }
                }
                try {
                    var heartbeat = item == HEARTBEAT;
                    if (!mayStillReceive(heartbeat)) {
                        logger.info("{} Closing the event stream of {}: the user may no longer view the project or "
                                            + "the credentials are no longer good", projectId(), userId);
                        end(true);
                        return;
                    }
                    if (heartbeat) {
                        sink.ping();
                    } else {
                        sink.send(epoch, (EventBatch) item);
                    }
                } catch (IOException | RuntimeException e) {
                    logger.debug("{} Closing the event stream of {}: {}", projectId(), userId, e.toString());
                    end(true);
                    return;
                }
            }
        }

        private boolean mayStillReceive(boolean force) {
            var now = System.nanoTime();
            if (!force && now - lastPermissionCheck < PERMISSION_CHECK_INTERVAL.toNanos()) {
                return true;
            }
            var allowed = sink.credentialsValid()
                    && accessManager.hasPermission(Subject.forUser(userId), ProjectResource.of(projectId()),
                                                   VIEW_PROJECT);
            lastPermissionCheck = now;
            return allowed;
        }

        /**
         * Ends the subscription at once; the sink, if it is to be closed, is closed on a sender thread, as closing
         * may wait for a send to a client that does not read.
         */
        private void end(boolean closeSink) {
            if (!ended.compareAndSet(false, true)) {
                return;
            }
            var handle = listener;
            if (handle != null) {
                try {
                    handle.close();
                } catch (Exception e) {
                    logger.warn("{} Could not remove an event listener", projectId(), e);
                }
            }
            subscriptions.remove(this);
            release(userId);
            synchronized (this) {
                queue.clear();
                heldBack.clear();
            }
            if (closeSink) {
                try {
                    senders.execute(this::closeSink);
                } catch (RejectedExecutionException e) {
                    closeSink();
                }
            }
        }

        private void closeSink() {
            try {
                sink.close();
            } catch (RuntimeException e) {
                logger.debug("{} Could not end an event stream of {}", projectId(), userId, e);
            }
        }
    }
}
