package org.industrial.ontology.app.event;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.entity.EntityService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ClassFrameChangedEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.PermissionsChangedEvent;
import org.industrial.ontology.kernel.event.EventBatch;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link ProjectEventService} and {@link ProjectEventBroadcaster} (07 5.1-8, 5.2-1) on an empty project. Heartbeats
 * come every 200 ms here and a user may open 3 streams; the tests of the queue limit use a broadcaster of their own
 * on which 3 batches may wait.
 */
class ProjectEventServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final UserId CAROL = UserId.getUserId("carol");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    private ProjectEventService events;

    private ProjectEventBroadcaster broadcaster;

    private EntityService entities;

    private ProjectRegistry registry;

    private ProjectTestFixture fixture;

    private ProjectId projectId;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory,
                                                                "webprotege.events.heartbeat=PT0.2S",
                                                                "webprotege.events.max-subscriptions-per-user=3");
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        fixture = new ProjectTestFixture(context);
        events = context.bean(ProjectEventService.class);
        broadcaster = context.bean(ProjectEventBroadcaster.class);
        entities = context.bean(EntityService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createProject(ALICE, "Events");
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    /**
     * 07 S8: when A creates a class, B's subscription receives the events within a second.
     */
    @Test
    void aChangeShouldReachASubscriberWithinASecond() throws Exception {
        var sink = new RecordingSink();
        var subscription = events.subscribe(BOB, projectId, null, sink);

        var outcome = createClass("Calzone");
        var started = System.nanoTime();
        var batch = sink.batches.poll(1, TimeUnit.SECONDS);
        var elapsed = Duration.ofNanos(System.nanoTime() - started);

        assertThat(batch).as("a batch within a second").isNotNull();
        assertThat(elapsed).isLessThan(Duration.ofSeconds(1));
        assertThat(batch.tag()).isEqualTo(outcome.eventTag());
        // 05 P1-09: the class's frame and its place in the hierarchy
        assertThat(batch.events()).hasAtLeastOneElementOfType(ClassFrameChangedEvent.class)
                                  .hasAtLeastOneElementOfType(EntityHierarchyChangedEvent.class);
        assertThat(sink.epochs).containsExactly(registry.get(projectId).events().epoch());
        assertThat(subscription.isOpen()).isTrue();
        subscription.close();
        assertThat(broadcaster.subscriptionCount()).isZero();
    }

    /**
     * 07 S8: resuming from a tag neither loses nor repeats a batch, also while changes go on.
     */
    @Test
    void catchingUpFromATagShouldNeitherMissNorRepeatABatch() throws Exception {
        var start = events.getEventsSince(ALICE, projectId, null);
        var from = new EventPosition(start.epoch(), start.tag());
        var changes = 30;
        var writer = Executors.newSingleThreadExecutor();
        var sinks = new ArrayList<RecordingSink>();
        var subscriptions = new ArrayList<ProjectEventBroadcaster.Subscription>();
        try {
            var written = writer.submit(() -> IntStream.rangeClosed(1, changes)
                                                       .forEach(i -> createClass("C" + i)));
            // Subscribe at different moments of the writing, all from the same position
            for (var i = 0; i < 3; i++) {
                var sink = new RecordingSink();
                sinks.add(sink);
                subscriptions.add(events.subscribe(BOB, projectId, from, sink));
                TimeUnit.MILLISECONDS.sleep(15);
            }
            written.get(60, TimeUnit.SECONDS);
            var end = events.getEventsSince(ALICE, projectId, null).tag();
            assertThat(end.getOrdinal() - start.tag().getOrdinal()).isEqualTo(changes);
            var expected = IntStream.rangeClosed(start.tag().getOrdinal() + 1, end.getOrdinal()).boxed().toList();
            for (var sink : sinks) {
                assertThat(sink.awaitTags(changes, Duration.ofSeconds(10))).containsExactlyElementsOf(expected);
            }
        } finally {
            writer.shutdownNow();
            subscriptions.forEach(ProjectEventBroadcaster.Subscription::close);
        }
    }

    /**
     * A batch that another thread posts while a subscription catches up is sent once, whenever it comes: once the
     * listener is registered it is both held back and read back, once the backlog has been read only held back.
     */
    @Test
    void aBatchPostedWhileCatchingUpShouldBeSentExactlyOnce() throws Exception {
        for (var step : ProjectEventBroadcaster.CatchUpStep.values()) {
            try (var own = ownBroadcaster()) {
                var projectEvents = registry.get(projectId).events();
                var start = projectEvents.getCurrentTag();
                var posted = new AtomicReference<EventTag>();
                own.setCatchUpProbe(at -> {
                    if (at == step) {
                        posted.set(projectEvents.postEvent(new PermissionsChangedEvent(projectId)));
                    }
                });
                var sink = new RecordingSink();

                own.subscribe(registry.get(projectId), BOB, EventPosition.of(start), sink);

                assertThat(posted.get()).as(step.name()).isEqualTo(start.next());
                assertThat(sink.awaitTags(1, Duration.ofSeconds(3))).as(step.name())
                                                                    .containsExactly(start.next().getOrdinal());
            }
        }
    }

    @Test
    void theEventsAfterATagShouldBeReadOnce() {
        var start = events.getEventsSince(BOB, projectId, null);
        assertThat(start.events()).isEmpty();
        assertThat(start.since()).isEqualTo(start.tag());
        assertThat(start.epoch()).matches("[0-9a-f]{8}");

        var outcome = createClass("Calzone");
        var after = events.getEventsSince(BOB, projectId, new EventPosition(start.epoch(), start.tag()));

        assertThat(after.since()).isEqualTo(start.tag());
        assertThat(after.tag()).isEqualTo(outcome.eventTag());
        assertThat(after.epoch()).isEqualTo(start.epoch());
        assertThat(after.events()).isNotEmpty();
        // A bare tag, such as that of the change, is read without the epoch
        var again = events.getEventsSince(BOB, projectId, EventPosition.of(outcome.eventTag()));
        assertThat(again.events()).isEmpty();
        assertThat(again.tag()).isEqualTo(after.tag());
    }

    @Test
    void aTagAheadOfTheProjectOrOfAnotherEpochShouldHaveExpired() {
        var current = events.getEventsSince(BOB, projectId, null);
        var ahead = EventPosition.of(EventTag.get(current.tag().getOrdinal() + 5));
        // The project has been loaded again since the client's tag: the same number means other events
        var otherEpoch = new EventPosition("0000beef".equals(current.epoch()) ? "0000cafe" : "0000beef",
                                           current.tag());

        for (var position : List.of(ahead, otherEpoch)) {
            assertThatThrownBy(() -> events.getEventsSince(BOB, projectId, position))
                    .isInstanceOf(WpException.class)
                    .satisfies(e -> {
                        assertThat(((WpException) e).getCode()).isEqualTo(ProjectEventService.EVENTS_EXPIRED);
                        assertThat(((WpException) e).getStatus()).isEqualTo(410);
                    });
            var sink = new RecordingSink();
            assertThatThrownBy(() -> events.subscribe(BOB, projectId, position, sink))
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo(ProjectEventService.EVENTS_EXPIRED);
            // The refused subscription holds nothing: no listener, no slot
            assertThat(broadcaster.subscriptionCount()).isZero();
            createClass("Calzone");
            assertThat(sink.batches).isEmpty();
            assertThat(sink.closed.getCount()).isOne();
        }
        var open = IntStream.range(0, 3).mapToObj(i -> events.subscribe(BOB, projectId, null, new RecordingSink()))
                            .toList();
        open.forEach(ProjectEventBroadcaster.Subscription::close);
    }

    /**
     * 05 P1-09: once the events after a tag are no longer kept, reading or streaming from it is refused with 410, so
     * that the client reloads instead of missing them.
     */
    @Test
    void aTagWhoseLaterEventsAreNoLongerKeptShouldHaveExpired(@TempDir Path otherDataDirectory) throws Exception {
        try (var shortLived = MongoPersistenceTestContext.startWithProjects(otherDataDirectory,
                                                                            "webprotege.events.retention=PT0.3S")) {
            var otherFixture = new ProjectTestFixture(shortLived);
            var otherProject = otherFixture.createProject(ALICE, "Short-lived events");
            var otherEvents = shortLived.bean(ProjectEventService.class);
            var start = EventPosition.of(otherEvents.getEventsSince(ALICE, otherProject, null).tag());
            shortLived.bean(EntityService.class)
                      .createEntities(ALICE, otherProject, EntityType.CLASS, "Calzone", "en", Set.of(), null);
            assertThat(otherEvents.getEventsSince(ALICE, otherProject, start).events()).isNotEmpty();

            // Purged after the retention, at the latest one retention period later
            TimeUnit.MILLISECONDS.sleep(1000);

            assertThatThrownBy(() -> otherEvents.getEventsSince(ALICE, otherProject, start))
                    .isInstanceOf(WpException.class)
                    .satisfies(e -> {
                        assertThat(((WpException) e).getCode()).isEqualTo(ProjectEventService.EVENTS_EXPIRED);
                        assertThat(((WpException) e).getStatus()).isEqualTo(410);
                    });
            assertThatThrownBy(() -> otherEvents.subscribe(ALICE, otherProject, start, new RecordingSink()))
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo(ProjectEventService.EVENTS_EXPIRED);
            // From the current tag all is well
            var current = EventPosition.of(otherEvents.getEventsSince(ALICE, otherProject, null).tag());
            assertThat(otherEvents.getEventsSince(ALICE, otherProject, current).events()).isEmpty();
        }
    }

    @Test
    void aClientTooFarBehindShouldReloadInsteadOfCatchingUp() {
        try (var own = ownBroadcaster()) {
            var ownEvents = new ProjectEventService(context.bean(ProjectContexts.class), own);
            var start = EventPosition.of(ownEvents.getEventsSince(BOB, projectId, null).tag());
            IntStream.rangeClosed(1, 3).forEach(i -> createClass("C" + i));
            assertThat(ownEvents.getEventsSince(BOB, projectId, start).events()).isNotEmpty();
            createClass("C4");

            assertThatThrownBy(() -> ownEvents.getEventsSince(BOB, projectId, start))
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo(ProjectEventService.EVENTS_EXPIRED);
            assertThatThrownBy(() -> ownEvents.subscribe(BOB, projectId, start, new RecordingSink()))
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo(ProjectEventService.EVENTS_EXPIRED);
        }
    }

    @Test
    void subscribersShouldBePingedAndLoseTheStreamWithTheirPermission() throws Exception {
        var sink = new RecordingSink();
        var subscription = events.subscribe(BOB, projectId, null, sink);
        assertThat(sink.awaitPings(2, Duration.ofSeconds(3))).isTrue();

        context.bean(AccessManager.class).setAssignedRoles(Subject.forUser(BOB), ProjectResource.of(projectId),
                                                           List.of());

        assertThat(sink.closed.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(subscription.isOpen()).isFalse();
        assertThat(broadcaster.subscriptionCount()).isZero();
        createClass("Calzone");
        assertThat(sink.batches.poll(300, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void aStreamShouldEndWhenItsCredentialsAreNoLongerGood() throws Exception {
        var sink = new RecordingSink();
        var subscription = events.subscribe(BOB, projectId, null, sink);
        assertThat(sink.awaitPings(1, Duration.ofSeconds(3))).isTrue();

        sink.credentialsValid.set(false);

        assertThat(sink.closed.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(subscription.isOpen()).isFalse();
    }

    @Test
    void aStreamShouldEndWhenItsProjectIsClosed() throws Exception {
        var sink = new RecordingSink();
        var subscription = events.subscribe(BOB, projectId, null, sink);

        registry.close(projectId);

        assertThat(sink.closed.await(3, TimeUnit.SECONDS)).isTrue();
        assertThat(subscription.isOpen()).isFalse();
    }

    @Test
    void anOpenStreamShouldKeepItsProjectLoaded() throws Exception {
        var loaded = registry.get(projectId);
        var sink = new RecordingSink();
        var subscription = events.subscribe(BOB, projectId, null, sink);
        var lastUse = loaded.lastAccess();

        assertThat(sink.awaitPings(2, Duration.ofSeconds(3))).isTrue();

        assertThat(loaded.lastAccess()).isAfter(lastUse);
        assertThat(registry.getIfLoaded(projectId)).containsSame(loaded);
        subscription.close();
    }

    @Test
    void aUserShouldHaveAtMostTheConfiguredNumberOfStreams() {
        var open = new ArrayList<ProjectEventBroadcaster.Subscription>();
        try {
            for (var i = 0; i < 3; i++) {
                open.add(events.subscribe(BOB, projectId, null, new RecordingSink()));
            }
            assertThatThrownBy(() -> events.subscribe(BOB, projectId, null, new RecordingSink()))
                    .isInstanceOf(WpException.class)
                    .satisfies(e -> {
                        assertThat(((WpException) e).getCode())
                                .isEqualTo(ProjectEventBroadcaster.TOO_MANY_SUBSCRIPTIONS);
                        assertThat(((WpException) e).getStatus()).isEqualTo(429);
                    });
            // Another user has streams of their own
            open.add(events.subscribe(ALICE, projectId, null, new RecordingSink()));
            open.remove(0).close();
            open.add(events.subscribe(BOB, projectId, null, new RecordingSink()));
            assertThat(broadcaster.subscriptionCount()).isEqualTo(4);
        } finally {
            open.forEach(ProjectEventBroadcaster.Subscription::close);
        }
    }

    @Test
    void aClientThatFallsBehindShouldLoseItsStreamWithoutHoldingUpChanges() throws Exception {
        try (var own = ownBroadcaster()) {
            var sink = new RecordingSink();
            sink.blockSending();
            var subscription = own.subscribe(registry.get(projectId), BOB, null, sink);
            try {
                // One batch is being sent, three wait, the next overflows
                var started = System.nanoTime();
                for (var i = 1; i <= 6; i++) {
                    createClass("C" + i);
                }
                assertThat(Duration.ofNanos(System.nanoTime() - started)).isLessThan(Duration.ofSeconds(5));
                assertThat(sink.closed.await(3, TimeUnit.SECONDS)).isTrue();
                assertThat(subscription.isOpen()).isFalse();
            } finally {
                sink.unblockSending();
            }
        }
    }

    @Test
    void readingAndStreamingShouldNeedViewProject() {
        assertThatThrownBy(() -> events.getEventsSince(CAROL, projectId, null))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> events.subscribe(CAROL, projectId, null, new RecordingSink()))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> events.getEventsSince(ALICE, ProjectId.getNil(), null))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThat(broadcaster.subscriptionCount()).isZero();
    }

    @Test
    void closingTheBroadcasterShouldEndEveryStream() throws Exception {
        var own = ownBroadcaster();
        var sink = new RecordingSink();
        own.subscribe(registry.get(projectId), BOB, null, sink);

        own.close();

        assertThat(sink.closed.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(own.subscriptionCount()).isZero();
        assertThatThrownBy(() -> own.subscribe(registry.get(projectId), BOB, null, new RecordingSink()))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * A broadcaster without heartbeats in the test, on which 3 batches may wait.
     */
    private ProjectEventBroadcaster ownBroadcaster() {
        return new ProjectEventBroadcaster(registry, context.bean(AccessManager.class), Duration.ofMinutes(1), 3, 3);
    }

    private ChangeOutcome<List<EntityNode>> createClass(String name) {
        return entities.createEntities(ALICE, projectId, EntityType.CLASS, name, "en", Set.of(), null);
    }

    private static final class RecordingSink implements ProjectEventSink {

        private final BlockingQueue<EventBatch> batches = new LinkedBlockingQueue<>();

        private final Set<String> epochs = new CopyOnWriteArraySet<>();

        private final AtomicInteger pings = new AtomicInteger();

        private final AtomicBoolean credentialsValid = new AtomicBoolean(true);

        private final CountDownLatch closed = new CountDownLatch(1);

        private volatile CountDownLatch sendingBlocked;

        @Override
        public void send(@Nonnull String epoch, @Nonnull EventBatch batch) {
            var blocked = sendingBlocked;
            if (blocked != null) {
                try {
                    blocked.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            epochs.add(epoch);
            batches.add(batch);
        }

        @Override
        public void ping() {
            pings.incrementAndGet();
        }

        @Override
        public boolean credentialsValid() {
            return credentialsValid.get();
        }

        @Override
        public void close() {
            closed.countDown();
        }

        void blockSending() {
            sendingBlocked = new CountDownLatch(1);
        }

        void unblockSending() {
            sendingBlocked.countDown();
        }

        boolean awaitPings(int count, Duration timeout) throws InterruptedException {
            var deadline = System.nanoTime() + timeout.toNanos();
            while (pings.get() < count && System.nanoTime() < deadline) {
                TimeUnit.MILLISECONDS.sleep(20);
            }
            return pings.get() >= count;
        }

        List<Integer> awaitTags(int count, Duration timeout) throws InterruptedException {
            var tags = new ArrayList<Integer>();
            var deadline = System.nanoTime() + timeout.toNanos();
            while (tags.size() < count) {
                var batch = batches.poll(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS);
                if (batch == null) {
                    break;
                }
                tags.add(batch.tag().getOrdinal());
            }
            // Nothing more must come: a repeated batch would arrive here
            var extra = batches.poll(200, TimeUnit.MILLISECONDS);
            if (extra != null) {
                tags.add(extra.tag().getOrdinal());
            }
            return tags;
        }
    }
}
