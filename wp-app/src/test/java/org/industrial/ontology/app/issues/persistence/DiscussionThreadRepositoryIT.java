package org.industrial.ontology.app.issues.persistence;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.CommentId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DiscussionThreadRepositoryIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl();

    private static final OWLClass PIZZA = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Pizza"));

    private static final OWLClass TOPPING = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Topping"));

    private static MongoPersistenceTestContext context;

    private DiscussionThreadRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        repository = context.bean(DiscussionThreadRepository.class);
    }

    @Test
    void shouldListTheThreadsOfAnEntityMostRecentlyStartedFirst() {
        repository.saveThread(thread("t1", PROJECT, PIZZA, Status.OPEN, comment("c1", 1000)));
        repository.saveThread(thread("t2", PROJECT, PIZZA, Status.CLOSED, comment("c2", 3000)));
        repository.saveThread(thread("t3", PROJECT, PIZZA, Status.OPEN, comment("c3", 2000), comment("c4", 4000)));
        repository.saveThread(thread("t4", PROJECT, TOPPING, Status.OPEN, comment("c5", 5000)));
        repository.saveThread(thread("t5", OTHER_PROJECT, PIZZA, Status.OPEN, comment("c6", 6000)));

        assertThat(repository.findThreads(PROJECT, PIZZA)).extracting(thread -> thread.getId().getId())
                                                          .containsExactly("t2", "t3", "t1");
        assertThat(repository.getCommentsCount(PROJECT, PIZZA)).isEqualTo(4);
        assertThat(repository.getOpenCommentsCount(PROJECT, PIZZA)).isEqualTo(3);
        assertThat(repository.getThreadsInProject(OTHER_PROJECT)).hasSize(1);
    }

    @Test
    void shouldAddUpdateAndDeleteComments() {
        repository.saveThread(thread("t1", PROJECT, PIZZA, Status.OPEN, comment("c1", 1000)));
        var threadId = new ThreadId("t1");

        repository.addCommentToThread(threadId, comment("c2", 2000));
        var edited = new Comment(CommentId.fromString("c1"), ALICE, 1000, Optional.of(3000L), "Edited",
                                 "<p>Edited</p>");
        repository.updateComment(threadId, edited);

        assertThat(repository.getThread(threadId).orElseThrow().getComments())
                .containsExactly(edited, comment("c2", 2000));
        assertThat(repository.findThreadByCommentId(CommentId.fromString("c2"))).map(EntityDiscussionThread::getId)
                                                                              .contains(threadId);

        assertThat(repository.deleteComment(CommentId.fromString("c2"))).isTrue();
        assertThat(repository.deleteComment(CommentId.fromString("c2"))).isFalse();
        assertThat(repository.getThread(threadId).orElseThrow().getComments()).containsExactly(edited);
    }

    @Test
    void shouldStoreCommentIdsAsUnderscoreId() {
        repository.saveThread(thread("t1", PROJECT, PIZZA, Status.OPEN, comment("c1", 1000)));
        repository.addCommentToThread(new ThreadId("t1"), comment("c2", 2000));

        var stored = context.database().getCollection(DiscussionThreadDocument.COLLECTION).find().first();
        assertThat(stored.getList("comments", org.bson.Document.class))
                .allSatisfy(comment -> assertThat(comment).containsKey("_id").doesNotContainKeys("id", "_class"));
    }

    @Test
    void shouldChangeTheStatusOfAThread() {
        repository.saveThread(thread("t1", PROJECT, PIZZA, Status.OPEN, comment("c1", 1000)));

        assertThat(repository.setThreadStatus(new ThreadId("t1"), Status.CLOSED)).map(EntityDiscussionThread::getStatus)
                                                                                .contains(Status.CLOSED);
        assertThat(repository.getOpenCommentsCount(PROJECT, PIZZA)).isZero();
        assertThat(repository.setThreadStatus(new ThreadId("missing"), Status.CLOSED)).isEmpty();
    }

    @Test
    void shouldMoveTheThreadsOfAMergedEntity() {
        repository.saveThread(thread("t1", PROJECT, TOPPING, Status.OPEN, comment("c1", 1000)));
        repository.saveThread(thread("t2", OTHER_PROJECT, TOPPING, Status.OPEN, comment("c2", 1000)));

        repository.replaceEntity(PROJECT, TOPPING, PIZZA);

        assertThat(repository.findThreads(PROJECT, PIZZA)).hasSize(1);
        assertThat(repository.findThreads(PROJECT, TOPPING)).isEmpty();
        assertThat(repository.findThreads(OTHER_PROJECT, TOPPING)).hasSize(1);
    }

    private static EntityDiscussionThread thread(String id,
                                                 ProjectId projectId,
                                                 OWLClass entity,
                                                 Status status,
                                                 Comment... comments) {
        return new EntityDiscussionThread(new ThreadId(id), projectId, entity, status, ImmutableList.copyOf(comments));
    }

    private static Comment comment(String id, long createdAt) {
        return new Comment(CommentId.fromString(id), ALICE, createdAt, Optional.empty(), "Body " + id,
                           "<p>Body " + id + "</p>");
    }
}
