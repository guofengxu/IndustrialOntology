package org.industrial.ontology.app.watch.persistence;

import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.watches.Watch;
import org.industrial.ontology.domain.watches.WatchType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WatchPersistenceIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl();

    private static final OWLClass PIZZA = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Pizza"));

    private static final OWLClass TOPPING = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Topping"));

    private static MongoPersistenceTestContext context;

    private WatchRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void watch() {
        context.clear();
        repository = context.bean(WatchRepository.class);
        repository.saveWatch(WatchDocument.of(PROJECT, new Watch(ALICE, PIZZA, WatchType.BRANCH)));
        repository.saveWatch(WatchDocument.of(PROJECT, new Watch(BOB, PIZZA, WatchType.ENTITY)));
        repository.saveWatch(WatchDocument.of(PROJECT, new Watch(ALICE, TOPPING, WatchType.ENTITY)));
        repository.saveWatch(WatchDocument.of(OTHER_PROJECT, new Watch(ALICE, PIZZA, WatchType.ENTITY)));
    }

    @Test
    void shouldFindWatchesByUserAndByEntity() {
        assertThat(repository.findWatches(PROJECT, ALICE)).extracting(WatchDocument::entity)
                                                          .containsExactlyInAnyOrder(PIZZA, TOPPING);
        assertThat(repository.findWatches(PROJECT, List.of(PIZZA))).extracting(WatchDocument::userId)
                                                                   .containsExactlyInAnyOrder("alice", "bob");
        assertThat(repository.findWatches(PROJECT, BOB, List.of(PIZZA, TOPPING))).extracting(WatchDocument::toWatch)
                .containsExactly(new Watch(BOB, PIZZA, WatchType.ENTITY));
    }

    @Test
    void shouldKeepOneWatchPerUserAndEntity() {
        repository.saveWatch(WatchDocument.of(PROJECT, new Watch(ALICE, PIZZA, WatchType.ENTITY)));

        assertThat(repository.findWatches(PROJECT, ALICE, List.of(PIZZA))).extracting(WatchDocument::type)
                                                                          .containsExactly(WatchType.ENTITY);
    }

    @Test
    void shouldDeleteAWatchOnlyWithItsType() {
        repository.deleteWatch(WatchDocument.of(PROJECT, new Watch(ALICE, PIZZA, WatchType.ENTITY)));
        assertThat(repository.findWatches(PROJECT, ALICE, List.of(PIZZA))).hasSize(1);

        repository.deleteWatch(WatchDocument.of(PROJECT, new Watch(ALICE, PIZZA, WatchType.BRANCH)));
        assertThat(repository.findWatches(PROJECT, ALICE, List.of(PIZZA))).isEmpty();
    }

    @Test
    void shouldGiveTheKernelTheDirectWatchesOfOneProject() {
        var watchManager = new MongoWatchManager(PROJECT, repository);

        assertThat(watchManager.getDirectWatches(PIZZA)).containsExactlyInAnyOrder(
                new Watch(ALICE, PIZZA, WatchType.BRANCH), new Watch(BOB, PIZZA, WatchType.ENTITY));
        assertThat(watchManager.getDirectWatches(PIZZA, BOB)).containsExactly(new Watch(BOB, PIZZA, WatchType.ENTITY));
    }
}
