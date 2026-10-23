package org.industrial.ontology.kernel.event;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.PermissionsChangedEvent;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ProjectEventManager}: tagged batches for the event streams of stage S8.
 */
class ProjectEventManagerTest {

    private static final ProjectId PROJECT = ProjectId.get("0b6f3cb0-6ec1-4cb6-9ad4-0a1c5d8f3e21");

    private static final Duration RETENTION = Duration.ofMinutes(10);

    private final AtomicLong clock = new AtomicLong(1_000_000L);

    private ScheduledExecutorService purges;

    private ProjectEventManager events;

    @BeforeEach
    void setUp() {
        purges = Executors.newSingleThreadScheduledExecutor();
        events = new ProjectEventManager(PROJECT, RETENTION, 3, purges, clock::get);
    }

    @AfterEach
    void tearDown() {
        events.dispose();
        purges.shutdownNow();
    }

    @Test
    void listenersShouldReceiveEveryBatchWithItsTagInTagOrderWhenPostsRace() throws Exception {
        var received = Collections.synchronizedList(new ArrayList<EventBatch>());
        events.addListener(received::add);
        var posters = Executors.newFixedThreadPool(8);
        try {
            var tasks = new ArrayList<Callable<List<EventTag>>>();
            for (var thread = 0; thread < 8; thread++) {
                tasks.add(() -> IntStream.range(0, 250).mapToObj(i -> events.postEvent(event())).toList());
            }
            for (var future : posters.invokeAll(tasks)) {
                future.get();
            }
        } finally {
            posters.shutdownNow();
        }

        assertThat(received).extracting(batch -> batch.tag().getOrdinal())
                            .containsExactlyElementsOf(IntStream.rangeClosed(1, 2000).boxed().toList());
        assertThat(received).allSatisfy(batch -> assertThat(batch.events()).hasSize(1));
        assertThat(events.getCurrentTag()).isEqualTo(EventTag.get(2000));
    }

    @Test
    void theBatchesAfterATagShouldBeReadBackWithTheirTags() {
        var first = event();
        var second = event();
        events.postEvents(List.of(first, first, second));
        var third = event();
        events.postEvent(third);

        assertThat(events.getEventBatchesAfter(EventTag.getFirst()))
                .containsExactly(new EventBatch(EventTag.get(1), List.of(first, second)),
                                 new EventBatch(EventTag.get(2), List.of(third)));
        assertThat(events.getEventBatchesAfter(EventTag.get(1)))
                .containsExactly(new EventBatch(EventTag.get(2), List.of(third)));
        assertThat(events.getEventBatchesAfter(EventTag.get(2))).isEmpty();
        assertThat(events.getEventsFromTag(EventTag.get(1)).getEvents()).containsExactly(first, second, third);
    }

    @Test
    void batchesOlderThanTheRetentionShouldBeGoneAndSaySo() {
        events.postEvent(event());
        clock.addAndGet(RETENTION.toMillis() / 2);
        var kept = event();
        events.postEvent(kept);
        clock.addAndGet(RETENTION.toMillis() / 2 + 1);

        events.removeExpiredEvents();

        assertThat(events.isRetainedFrom(EventTag.get(1))).isFalse();
        assertThat(events.isRetainedFrom(EventTag.get(2))).isTrue();
        assertThat(events.getEventBatchesAfter(EventTag.getFirst()))
                .containsExactly(new EventBatch(EventTag.get(2), List.of(kept)));
    }

    @Test
    void aBatchLargerThanTheLimitShouldBeNeitherRecordedNorDelivered() {
        var received = new ArrayList<EventBatch>();
        events.addListener(received::add);

        var tag = events.postEvents(List.of(event(), event(), event(), event()));

        assertThat(tag).isEqualTo(EventTag.getFirst());
        assertThat(received).isEmpty();
        assertThat(events.getEventBatchesAfter(EventTag.getFirst())).isEmpty();
    }

    @Test
    void aRemovedListenerShouldReceiveNothingMore() throws Exception {
        var received = new ArrayList<EventBatch>();
        var registration = events.addListener(received::add);
        events.postEvent(event());
        registration.close();
        events.postEvent(event());
        assertThat(received).extracting(EventBatch::tag).containsExactly(EventTag.get(1));
    }

    @Test
    void aFailingListenerShouldNotStopTheOthers() {
        var received = new ArrayList<EventBatch>();
        events.addListener(batch -> {
            throw new IllegalStateException("Listener failure");
        });
        events.addListener(received::add);
        events.postEvent(event());
        assertThat(received).hasSize(1);
    }

    private static ProjectEvent event() {
        return new PermissionsChangedEvent(ProjectId.get(UUID.randomUUID().toString()));
    }
}
