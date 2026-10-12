package org.industrial.ontology.api.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.project.UploadService;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The revision and event API (docs/02 §5, 07 S8) over HTTP on the pizza project: the acceptance flows of 05 P1-08 and
 * P1-09 and the JSON of every endpoint. Streams are checked every second here.
 */
@TestPropertySource(properties = "webprotege.events.heartbeat=PT1S")
class RevisionsAndEventsIT extends ApiIntegrationTest {

    private static final String EDITOR = "editor";

    private static final String VIEWER = "viewer";

    private static final String PIZZA = PizzaOntology.NAMESPACE;

    private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ApiKeyService apiKeyService;

    private String projectPath;

    @BeforeEach
    void createPizzaProject() throws IOException {
        var editor = Subject.forUser(EDITOR);
        var roles = new LinkedHashSet<>(accessManager.getAssignedRoles(editor, ApplicationResource.get()));
        roles.add(BuiltInRole.PROJECT_CREATOR.getRoleId());
        roles.add(BuiltInRole.PROJECT_UPLOADER.getRoleId());
        accessManager.setAssignedRoles(editor, ApplicationResource.get(), roles);
        var file = PizzaOntology.copyTo(Files.createTempDirectory("pizza"));
        ProjectId projectId;
        try (var content = Files.newInputStream(file)) {
            var documentId = uploadService.upload(UserId.getUserId(EDITOR), "pizza.owl", content).documentId();
            projectId = projectService.createProject(UserId.getUserId(EDITOR), "Pizza", "", "en", documentId)
                                      .getProjectId();
        }
        accessManager.setAssignedRoles(Subject.forUser(VIEWER), ProjectResource.of(projectId),
                                       List.of(BuiltInRole.CAN_VIEW.getRoleId()));
        projectPath = "/api/v1/projects/" + projectId.getId();
    }

    /**
     * 07 S8 and 05 P1-09: B's stream receives the class A creates within a second; after a disconnect B resumes from
     * the last id it received and gets what it missed, nothing twice.
     */
    @Test
    void aStreamShouldReceiveAnotherUsersChangeWithinASecondAndResumeWithoutGapsOrRepeats() throws Exception {
        String lastId;
        try (var stream = openStream(VIEWER, null)) {
            assertThat(stream.status).isEqualTo(200);
            assertThat(stream.contentType).startsWith("text/event-stream");

            var created = send(HttpMethod.POST, "/entities", EDITOR, create("Calzone"));
            var sent = Instant.now();
            assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            var eventTag = json(created).get("eventTag").asInt();

            var message = stream.nextBatch(Duration.ofSeconds(1));
            assertThat(message).as("the class's events within a second").isNotNull();
            assertThat(Duration.between(sent, Instant.now())).isLessThan(Duration.ofSeconds(1));
            assertThat(message.event()).isEqualTo("events");
            var data = objectMapper.readTree(message.data());
            assertThat(data.get("epoch").asText()).matches("[0-9a-f]{8}");
            assertThat(message.id()).isEqualTo(data.get("epoch").asText() + "-" + eventTag);
            assertThat(data.get("tag").asInt()).isEqualTo(eventTag);
            assertThat(data.get("events")).isNotEmpty();
            assertThat(data.get("events")).extracting(event -> event.get("type").asText())
                                          .contains("ClassFrameChanged", "EntityHierarchyChanged");
            assertThat(data.toString()).contains("Calzone");
            lastId = message.id();
        }
        var epoch = lastId.substring(0, lastId.indexOf('-'));

        // Missed while disconnected
        var missed = new ArrayList<String>();
        for (var name : List.of("Stromboli", "Panzerotto")) {
            missed.add(epoch + "-" + json(send(HttpMethod.POST, "/entities", EDITOR, create(name))).get("eventTag")
                                                                                                    .asText());
        }

        try (var stream = openStream(VIEWER, lastId)) {
            var ids = new ArrayList<String>();
            for (var i = 0; i < missed.size(); i++) {
                var message = stream.nextBatch(Duration.ofSeconds(5));
                assertThat(message).isNotNull();
                ids.add(message.id());
            }
            assertThat(ids).containsExactlyElementsOf(missed);
            assertThat(stream.nextBatch(Duration.ofMillis(500))).as("nothing repeated").isNull();
        }

        // The same with the pull endpoint
        var since = json(send(HttpMethod.GET, "/events?since=" + lastId, VIEWER, null));
        assertThat(since.get("epoch").asText()).isEqualTo(epoch);
        assertThat(epoch + "-" + since.get("since").asText()).isEqualTo(lastId);
        assertThat(since.get("position").asText()).isEqualTo(missed.get(1));
        assertThat(since.get("events").toString()).contains("Stromboli").contains("Panzerotto")
                                                  .doesNotContain("Calzone");
        var current = json(send(HttpMethod.GET, "/events", VIEWER, null));
        assertThat(current.get("position").asText()).isEqualTo(missed.get(1));
        assertThat(current.get("events")).isEmpty();
        // A bare tag, such as the event tag of a change, resumes too
        var bare = json(send(HttpMethod.GET, "/events?since=" + lastId.substring(epoch.length() + 1), VIEWER, null));
        assertThat(bare.get("position").asText()).isEqualTo(missed.get(1));
        // A tag of another load of the project does not
        try (var stream = openStream(VIEWER, ("0000beef".equals(epoch) ? "0000cafe" : "0000beef")
                + lastId.substring(epoch.length()))) {
            assertThat(stream.status).isEqualTo(410);
            assertThat(stream.body()).contains("EVENTS_EXPIRED");
        }
    }

    @Test
    void tagsThatCannotBeResumedShouldBeRefused() throws Exception {
        var expired = send(HttpMethod.GET, "/events?since=99999", VIEWER, null);
        assertThat(expired.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(json(expired).get("code").asText()).isEqualTo("EVENTS_EXPIRED");
        try (var stream = openStream(VIEWER, "99999")) {
            assertThat(stream.status).isEqualTo(410);
            assertThat(stream.body()).contains("EVENTS_EXPIRED");
        }
        for (var bad : List.of("-1", "abc", "x1-2", "1-", "99999999999")) {
            var refused = send(HttpMethod.GET, "/events?since=" + bad, VIEWER, null);
            assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(json(refused).get("code").asText()).isEqualTo("INVALID_REQUEST");
        }
        try (var stream = openStream(null, null)) {
            assertThat(stream.status).isEqualTo(401);
        }
        try (var stream = openStream("admin", null)) {
            assertThat(stream.status).isEqualTo(403);
        }
    }

    /**
     * A stream opened with an API key ends once the key is revoked (S8 security review): the stream is checked
     * every heartbeat, not only when it is opened.
     */
    @Test
    void aStreamOpenedWithAnApiKeyShouldEndOnceTheKeyIsRevoked() throws Exception {
        // A user of its own, who keeps another key: revoking a user's last key leaves an empty key list, on which the
        // in-memory Mongo then refuses the other test classes' new keys (see 07, round 10)
        var reader = UserId.getUserId("stream-reader");
        var projectId = ProjectId.get(projectPath.substring(projectPath.lastIndexOf('/') + 1));
        accessManager.setAssignedRoles(Subject.forUser(reader), ProjectResource.of(projectId),
                                       List.of(BuiltInRole.CAN_VIEW.getRoleId()));
        apiKeyService.generateApiKeyForUser(reader, "kept");
        var generated = apiKeyService.generateApiKeyForUser(reader, "events");
        try (var stream = openStreamAs("ApiKey " + generated.apiKey().getKey(), null)) {
            assertThat(stream.status).isEqualTo(200);
            assertThat(stream.awaitEnd(Duration.ofSeconds(2))).as("open while the key is good").isFalse();

            apiKeyService.revokeApiKey(reader, generated.details().apiKeyId());

            assertThat(stream.awaitEnd(Duration.ofSeconds(5))).as("ended after the next heartbeat").isTrue();
        }
    }

    /**
     * 07 S8 and 05 P1-08: reverting the revision of a frame edit restores the frame.
     */
    @Test
    void revertingARevisionShouldRestoreTheFrame() {
        var path = "/frames/class?iri=" + encode(PIZZA + "Margherita");
        var original = json(send(HttpMethod.GET, path, VIEWER, null)).get("frame");
        var edited = json(send(HttpMethod.PUT, path, EDITOR, update(original, withComment(original, "Basil"))));
        var number = edited.get("revision").get("number").asLong();

        var viewerRevert = send(HttpMethod.POST, "/revisions/" + number + "/revert", VIEWER, null);
        assertThat(viewerRevert.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        var reverted = send(HttpMethod.POST, "/revisions/" + number + "/revert", EDITOR,
                            objectMapper.createObjectNode().put("commitMessage", "No basil"));

        assertThat(reverted.getStatusCode()).isEqualTo(HttpStatus.OK);
        var outcome = json(reverted);
        assertThat(outcome.get("result").get("reverted").asLong()).isEqualTo(number);
        assertThat(outcome.get("revision").get("number").asLong()).isEqualTo(number + 1);
        assertThat(outcome.get("eventTag").isInt()).isTrue();
        assertThat(json(send(HttpMethod.GET, path, VIEWER, null)).get("frame")).isEqualTo(original);
        var revert = json(send(HttpMethod.GET, "/revisions/" + (number + 1), VIEWER, null));
        assertThat(revert.get("description").asText()).isEqualTo("No basil");
        assertThat(revert.get("changes").get(0).get("operation").asText()).isEqualTo("REMOVE");
    }

    @Test
    void revisionsShouldBeListedAndShown() {
        var path = "/frames/class?iri=" + encode(PIZZA + "Margherita");
        var frame = json(send(HttpMethod.GET, path, VIEWER, null)).get("frame");
        send(HttpMethod.PUT, path, EDITOR, update(frame, withComment(frame, "Tomato")));

        var head = json(send(HttpMethod.GET, "/revisions/head", VIEWER, null)).get("number").asLong();
        var revisions = json(send(HttpMethod.GET, "/revisions", VIEWER, null));
        assertThat(revisions).hasSize((int) head);
        var latest = revisions.get((int) head - 1);
        assertThat(latest.get("number").asLong()).isEqualTo(head);
        assertThat(latest.get("userId").asText()).isEqualTo(EDITOR);
        assertThat(latest.get("changeCount").asInt()).isEqualTo(1);
        assertThat(Instant.parse(latest.get("timestamp").asText())).isBefore(Instant.now().plusSeconds(1));
        assertThat(json(send(HttpMethod.GET, "/revisions?from=" + head + "&userId=" + EDITOR, VIEWER, null)))
                .hasSize(1);
        assertThat(json(send(HttpMethod.GET, "/revisions?from=" + head + "&userId=someone", VIEWER, null)))
                .isEmpty();

        var details = json(send(HttpMethod.GET, "/revisions/" + head, VIEWER, null));
        assertThat(details.get("number").asLong()).isEqualTo(head);
        assertThat(details.get("subjects")).extracting(JsonNode::asText).containsExactly(PIZZA + "Margherita");
        assertThat(details.get("changes").get(0).get("operation").asText()).isEqualTo("ADD");
        assertThat(details.get("changes").get(0).get("axiom").asText()).contains("Tomato");
        assertThat(details.get("tags")).isEmpty();
        var diff = json(send(HttpMethod.GET, "/revisions/diff?from=" + (head - 1) + "&to=" + head, VIEWER, null));
        assertThat(diff.get("addedCount").asInt()).isOne();
        assertThat(diff.get("addedAxioms").get(0).asText()).contains("Margherita").contains("Tomato");
        assertThat(diff.get("removedAxioms")).isEmpty();
        assertThat(send(HttpMethod.GET, "/revisions/diff?to=" + (head + 1), VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        var imported = json(send(HttpMethod.GET, "/revisions/1", VIEWER, null));
        assertThat(imported.get("changeCount").asInt()).isGreaterThan(imported.get("changes").size() - 1);

        var missing = send(HttpMethod.GET, "/revisions/" + (head + 100), VIEWER, null);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(json(missing).get("code").asText()).isEqualTo("REVISION_NOT_FOUND");
        for (var bad : List.of("/revisions/abc", "/revisions/0", "/revisions?from=0", "/revisions/abc/revert")) {
            var method = bad.endsWith("revert") ? HttpMethod.POST : HttpMethod.GET;
            assertThat(send(method, bad, EDITOR, null).getStatusCode()).as(bad).isEqualTo(HttpStatus.BAD_REQUEST);
        }
        assertThat(send(HttpMethod.GET, "/revisions", "admin", null).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * 07 S8: the changes of an entity are only that entity's changes; their HTML escapes literals.
     */
    @Test
    void theChangesOfAnEntityShouldBeOnlyItsChanges() {
        var margherita = "/frames/class?iri=" + encode(PIZZA + "Margherita");
        var frame = json(send(HttpMethod.GET, margherita, VIEWER, null)).get("frame");
        send(HttpMethod.PUT, margherita, EDITOR, update(frame, withComment(frame, "<b>Basil</b>")));
        var american = "/frames/class?iri=" + encode(PIZZA + "American");
        var other = json(send(HttpMethod.GET, american, VIEWER, null)).get("frame");
        send(HttpMethod.PUT, american, EDITOR, update(other, withComment(other, "Pepperoni")));

        var changes = json(send(HttpMethod.GET, "/changes?iri=" + encode(PIZZA + "Margherita") + "&type=Class",
                                VIEWER, null));

        var latest = changes.get("pageElements").get(0);
        assertThat(latest.get("userId").asText()).isEqualTo(EDITOR);
        assertThat(latest.get("changeCount").asInt()).isOne();
        var line = latest.get("diff").get("pageElements").get(0);
        assertThat(line.get("operation").asText()).isEqualTo("ADD");
        assertThat(line.get("html").asText()).contains("&lt;b&gt;Basil&lt;/b&gt;").doesNotContain("<b>Basil");
        assertThat(changes.toString()).doesNotContain("Pepperoni");

        var all = json(send(HttpMethod.GET, "/changes?size=2", VIEWER, null));
        assertThat(all.get("pageElements")).hasSize(2);
        assertThat(all.get("pageElements").get(0).toString()).contains("Pepperoni");
        assertThat(send(HttpMethod.GET, "/changes?type=Class", VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(send(HttpMethod.GET, "/changes?size=101", VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        // A page whose first element is beyond what an int counts
        assertThat(send(HttpMethod.GET, "/changes?page=2147483647&size=100", VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private SseStream openStream(@Nullable String user, @Nullable String lastEventId) throws Exception {
        return openStreamAs(user == null ? null : bearer(user), lastEventId);
    }

    private SseStream openStreamAs(@Nullable String authorization, @Nullable String lastEventId) throws Exception {
        var request = HttpRequest.newBuilder(URI.create(rest.getRootUri() + projectPath + "/events/stream"))
                                 .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
        if (authorization != null) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (lastEventId != null) {
            request.header("Last-Event-ID", lastEventId);
        }
        var response = HttpClient.newBuilder()
                                 .version(HttpClient.Version.HTTP_1_1)
                                 .build()
                                 .send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
        return new SseStream(response);
    }

    private ObjectNode create(String sourceText) {
        var request = objectMapper.createObjectNode()
                                  .put("entityType", "Class")
                                  .put("sourceText", sourceText)
                                  .put("langTag", "en");
        request.putArray("parents").addObject().put("type", "owl:Class").put("iri", PIZZA + "Pizza");
        return request;
    }

    private ObjectNode update(JsonNode from, JsonNode to) {
        var request = objectMapper.createObjectNode();
        request.set("from", from);
        request.set("to", to);
        return request;
    }

    private JsonNode withComment(JsonNode frame, String comment) {
        var to = (ObjectNode) frame.deepCopy();
        var values = (ArrayNode) to.get("propertyValues");
        var value = values.addObject().put("type", "PropertyAnnotationValue");
        value.putObject("property").put("type", "owl:AnnotationProperty").put("iri", RDFS_COMMENT);
        value.putObject("value").put("lang", "en").put("value", comment);
        return to;
    }

    private ResponseEntity<String> send(HttpMethod method, String path, String user, @Nullable Object body) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearer(user));
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return rest.exchange(URI.create(rest.getRootUri() + projectPath + path), method,
                             new HttpEntity<>(body, headers), String.class);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record SseMessage(@Nullable String id, @Nullable String event, String data) {
    }

    /**
     * Reads a server-sent event stream on a thread of its own; comments (pings) are skipped.
     */
    private static final class SseStream implements AutoCloseable {

        private final int status;

        private final String contentType;

        private final InputStream body;

        private final BlockingQueue<SseMessage> messages = new LinkedBlockingQueue<>();

        private final StringBuilder text = new StringBuilder();

        private final Thread reader;

        SseStream(HttpResponse<InputStream> response) {
            status = response.statusCode();
            contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse("");
            body = response.body();
            reader = new Thread(this::read, "sse-test-reader");
            reader.setDaemon(true);
            reader.start();
        }

        private void read() {
            try (var lines = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                String id = null;
                String event = null;
                var data = new StringBuilder();
                String line;
                while ((line = lines.readLine()) != null) {
                    synchronized (text) {
                        text.append(line).append('\n');
                    }
                    if (line.isEmpty()) {
                        if (data.length() > 0) {
                            messages.add(new SseMessage(id, event, data.toString()));
                        }
                        id = null;
                        event = null;
                        data.setLength(0);
                    } else if (line.startsWith("id:")) {
                        id = line.substring(3).strip();
                    } else if (line.startsWith("event:")) {
                        event = line.substring(6).strip();
                    } else if (line.startsWith("data:")) {
                        data.append(line.substring(5).strip());
                    }
                }
            } catch (IOException e) {
                // closed
            }
        }

        @Nullable
        SseMessage nextBatch(Duration timeout) throws InterruptedException {
            return messages.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        /**
         * Whether the server has ended the stream within the time.
         */
        boolean awaitEnd(Duration timeout) throws InterruptedException {
            reader.join(timeout.toMillis());
            return !reader.isAlive();
        }

        String body() throws InterruptedException {
            reader.join(5000);
            synchronized (text) {
                return text.toString();
            }
        }

        @Override
        public void close() {
            try {
                body.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
