package org.industrial.ontology.api.ontology;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The ontology editing and search API (docs/02 §4, 07 S7) over HTTP, on the pizza project: the acceptance flows of 05
 * P1-05 and P1-06, and the JSON of every endpoint.
 */
class OntologyEditingIT extends ApiIntegrationTest {

    private static final String EDITOR = "editor";

    private static final String VIEWER = "viewer";

    private static final String PIZZA = PizzaOntology.NAMESPACE;

    private static final String RDFS_COMMENT = "http://www.w3.org/2000/01/rdf-schema#comment";

    @Autowired
    private UploadService uploadService;

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
     * 05 P1-05: create a class below a parent, see it in the hierarchy, annotate it through its frame, conflict,
     * fail a Manchester syntax check with its line and column, merge, delete.
     */
    @Test
    void aClassShouldBeCreatedEditedMergedAndDeleted() {
        // Create a class below Pizza
        var created = send(HttpMethod.POST, "/entities", EDITOR,
                           create("Calzone", PIZZA + "Pizza"));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var calzone = json(created).get("result").get("entities").get(0);
        assertThat(calzone.get("displayName").asText()).isEqualTo("Calzone");
        assertThat(calzone.get("entity").get("type").asText()).isEqualTo("owl:Class");
        assertThat(json(created).get("revision").get("number").asLong()).isPositive();
        assertThat(json(created).get("revision").get("userId").asText()).isEqualTo(EDITOR);
        assertThat(json(created).get("eventTag").isInt()).isTrue();
        var calzoneIri = calzone.get("entity").get("iri").asText();

        // It is a child of Pizza
        var children = json(send(HttpMethod.GET, "/hierarchies/Class/children?iri=" + encode(PIZZA + "Pizza"),
                                 VIEWER, null));
        assertThat(children.get("parent").get("leaf").asBoolean()).isFalse();
        assertThat(iris(children.get("children").get("pageElements"))).contains(calzoneIri);

        // Annotate it through its frame
        var frame = json(send(HttpMethod.GET, "/frames/class?iri=" + encode(calzoneIri), VIEWER, null));
        assertThat(frame.get("frame").get("type").asText()).isEqualTo("ClassFrame");
        assertThat(frame.get("rendered").get("subject").get("displayName").asText()).isEqualTo("Calzone");
        var from = frame.get("frame");
        var to = withComment(from, "Folded");
        var updated = send(HttpMethod.PUT, "/frames/class?iri=" + encode(calzoneIri), EDITOR,
                           update(from, to, "Commented on the calzone"));
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(updated).get("result").get("frame")).isEqualTo(to);
        assertThat(json(updated).get("result").get("rendered").get("propertyValues").toString()).contains("Folded");

        // A second edit of the frame as first read conflicts
        var conflict = send(HttpMethod.PUT, "/frames/class?iri=" + encode(calzoneIri), EDITOR,
                            update(from, withComment(from, "Baked"), null));
        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(json(conflict).get("code").asText()).isEqualTo("CONFLICT_FRAME_CHANGED");

        // A Manchester syntax error is reported with its line and column
        var check = objectMapper.createObjectNode()
                                .put("iri", calzoneIri)
                                .put("type", "Class")
                                .put("from", "Class: Calzone")
                                .put("to", "Class: Calzone\n    SubClassOf: Nothingness");
        var checked = json(send(HttpMethod.POST, "/frames/manchester/check", VIEWER, check));
        assertThat(checked.get("status").asText()).isEqualTo("ERROR");
        assertThat(checked.get("ok").asBoolean()).isFalse();
        assertThat(checked.get("error").get("line").asInt()).isEqualTo(2);
        assertThat(checked.get("error").get("column").asInt()).isEqualTo(17);
        var set = objectMapper.createObjectNode()
                              .put("from", "Class: Calzone")
                              .put("to", "Class: Calzone\n    SubClassOf: Nothingness");
        var failed = send(HttpMethod.PUT, "/frames/manchester?iri=" + encode(calzoneIri) + "&type=Class", EDITOR,
                          set);
        assertThat(failed.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(failed).get("code").asText()).isEqualTo("MANCHESTER_SYNTAX_ERROR");
        assertThat(json(failed).get("line").asInt()).isEqualTo(2);
        assertThat(json(failed).get("column").asInt()).isEqualTo(17);

        // Merge another class into it
        var stromboli = json(send(HttpMethod.POST, "/entities", EDITOR, create("Stromboli", PIZZA + "Pizza")))
                .get("result").get("entities").get(0).get("entity");
        var merge = objectMapper.createObjectNode();
        merge.putArray("sourceEntities").add(stromboli);
        merge.set("targetEntity", calzone.get("entity"));
        merge.put("treatment", "DELETE");
        var merged = send(HttpMethod.POST, "/entities/merge", EDITOR, merge);
        assertThat(merged.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(merged).get("result").get("entity")).isEqualTo(calzone.get("entity"));
        assertThat(childrenOfPizza()).contains(calzoneIri).doesNotContain(stromboli.get("iri").asText());

        // Delete it
        var delete = objectMapper.createObjectNode();
        delete.putArray("entities").add(calzone.get("entity"));
        var deleted = send(HttpMethod.POST, "/entities/delete", EDITOR, delete);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(deleted).get("result").get("entities").get(0)).isEqualTo(calzone.get("entity"));
        assertThat(childrenOfPizza()).doesNotContain(calzoneIri);

        // Viewers may not edit
        var refused = send(HttpMethod.POST, "/entities", VIEWER, create("Pide", PIZZA + "Pizza"));
        assertThat(refused.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(json(refused).get("code").asText()).isEqualTo("PERMISSION_DENIED");
    }

    /**
     * 05 P1-06: "mozz" finds MozzarellaTopping where it matched.
     */
    @Test
    void mozzShouldFindMozzarellaTopping() {
        var request = objectMapper.createObjectNode().put("searchString", "mozz");
        request.putArray("entityTypes").add("Class");
        var results = json(send(HttpMethod.POST, "/entities/search", VIEWER, request));
        var first = results.get("pageElements").get(0);
        assertThat(first.get("entity").get("entity").get("iri").asText()).isEqualTo(PIZZA + "MozzarellaTopping");
        assertThat(first.get("matches")).anySatisfy(match -> {
            assertThat(match.get("value").asText()).isEqualTo("Mozzarella Topping");
            assertThat(match.get("positions").get(0).get("start").asInt()).isZero();
            assertThat(match.get("positions").get(0).get("end").asInt()).isEqualTo(4);
        });
        assertThat(results.get("pageNumber").asInt()).isEqualTo(1);

        var lookup = json(send(HttpMethod.GET, "/entities/lookup?q=mozz&types=Class", VIEWER, null));
        assertThat(lookup.get(0).get("entity").get("entity").get("iri").asText())
                .isEqualTo(PIZZA + "MozzarellaTopping");
        assertThat(lookup.get(0).get("match").get("language").has("type")).isTrue();
    }

    @Test
    void criteriaAndSearchFiltersShouldNarrowTheEntities() {
        var match = objectMapper.createObjectNode().put("page", 1).put("size", 5);
        match.putObject("criteria").put("match", "EntityTypeIsOneOf").putArray("types").add("owl:NamedIndividual");
        var individuals = json(send(HttpMethod.POST, "/entities/match", VIEWER, match));
        assertThat(individuals.get("pageElements")).hasSize(5)
                                                   .allSatisfy(node -> assertThat(node.get("entity").get("type")
                                                                                      .asText())
                                                           .isEqualTo("owl:NamedIndividual"));
        assertThat(individuals.get("totalElements").asLong()).isGreaterThan(5);

        var filters = objectMapper.createArrayNode();
        var cheeses = filters.addObject();
        cheeses.putObject("label").put("en", "Cheeses");
        cheeses.putObject("criteria")
               .put("match", "SubClassOf")
               .put("filterType", "ALL")
               .putObject("target").put("type", "owl:Class").put("iri", PIZZA + "CheeseTopping");
        var stored = send(HttpMethod.PUT, "/search-settings", EDITOR, filters);
        assertThat(stored.getStatusCode()).isEqualTo(HttpStatus.OK);
        var filterId = json(stored).get(0).get("id").asText();
        assertThat(json(send(HttpMethod.GET, "/search-settings", VIEWER, null))).isEqualTo(json(stored));
        assertThat(send(HttpMethod.PUT, "/search-settings", VIEWER, filters).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        var search = objectMapper.createObjectNode().put("searchString", "topping");
        search.putArray("filters").add(filterId);
        var found = iris(StreamSupport.stream(json(send(HttpMethod.POST, "/entities/search", VIEWER, search))
                                                      .get("pageElements").spliterator(), false)
                                      .map(result -> result.get("entity"))
                                      .toList());
        assertThat(found).contains(PIZZA + "MozzarellaTopping").doesNotContain(PIZZA + "TomatoTopping");
    }

    /**
     * 05 P1-05: {@code /axioms} in its three media types.
     */
    @Test
    void axiomsShouldBeAddedAndDeletedInEachMediaType() {
        var turtle = """
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                <%sCalzone> a owl:Class .
                """.formatted(PIZZA);
        var added = post("/axioms?commitMessage=Calzone", "text/turtle", turtle);
        assertThat(added.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(json(added).get("result").get("addedAxioms").asInt()).isEqualTo(1);
        var rdfXml = """
                <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                         xmlns:owl="http://www.w3.org/2002/07/owl#">
                    <owl:Class rdf:about="%sStromboli"/>
                </rdf:RDF>
                """.formatted(PIZZA);
        assertThat(json(post("/axioms", "application/rdf+xml", rdfXml)).get("result").get("addedAxioms").asInt())
                .isEqualTo(1);
        var functional = "Ontology(SubClassOf(<%1$sStromboli> <%1$sPizza>))".formatted(PIZZA);
        assertThat(json(post("/axioms", "text/owl-functional; charset=UTF-8", functional)).get("result")
                                                                                         .get("addedAxioms")
                                                                                         .asInt()).isEqualTo(1);
        var deleted = post("/axioms/delete", "text/owl-functional", functional);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(deleted).get("result").get("deletedAxioms").asInt()).isEqualTo(1);

        assertThat(post("/axioms", "application/json", "{}").getStatusCode())
                .isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        var unparsable = post("/axioms", "text/turtle", functional);
        assertThat(unparsable.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(unparsable).get("code").asText()).isEqualTo("UNPARSABLE_AXIOMS");
    }

    @Test
    void theOtherEndpointsShouldAnswerInTheirJson() {
        var margherita = encode(PIZZA + "Margherita");
        assertThat(json(send(HttpMethod.GET, "/hierarchies/class/roots", VIEWER, null)).get(0).get("entity")
                                                                                     .get("iri").asText())
                .isEqualTo("http://www.w3.org/2002/07/owl#Thing");
        var paths = json(send(HttpMethod.GET, "/hierarchies/Class/paths-to-root?iri=" + margherita, VIEWER, null));
        assertThat(paths.get(0).get(paths.get(0).size() - 1).get("displayName").asText()).isEqualTo("Margherita");
        assertThat(json(send(HttpMethod.GET, "/hierarchies/Class/siblings?iri=" + margherita, VIEWER, null))
                           .get("pageElements")).isNotEmpty();
        var moveFunghi = objectMapper.createObjectNode().put("dropType", "ADD");
        moveFunghi.putObject("entity").put("type", "owl:Class").put("iri", PIZZA + "Funghi");
        moveFunghi.putObject("toParent").put("type", "owl:Class").put("iri", PIZZA + "Pizza");
        assertThat(json(send(HttpMethod.POST, "/hierarchies/Class/move", EDITOR, moveFunghi)).get("result")
                                                                                            .get("moved")
                                                                                            .asBoolean())
                .isTrue();

        assertThat(json(send(HttpMethod.GET, "/entities/rendering?iri=" + margherita + "&type=Class", VIEWER,
                             null)).get("displayName").asText()).isEqualTo("Margherita");
        assertThat(json(send(HttpMethod.GET, "/entities/html?iri=" + margherita + "&type=owl:Class", VIEWER,
                             null)).get("html").asText()).contains("Margherita");
        var usage = json(send(HttpMethod.GET, "/entities/usage?iri=" + encode(PIZZA + "MozzarellaTopping")
                + "&type=Class&size=2", VIEWER, null));
        assertThat(usage.get("references").get("pageElements")).hasSize(2);
        assertThat(usage.get("axiomCount").asInt()).isGreaterThan(2);
        assertThat(iris(json(send(HttpMethod.GET, "/entities/deprecated?types=Class", VIEWER, null))
                                .get("pageElements"))).contains(PIZZA + "Veneziana");

        var individuals = json(send(HttpMethod.GET, "/individuals?type=" + margherita + "&mode=DIRECT", VIEWER,
                                    null));
        assertThat(individuals.get("type").get("displayName").asText()).isEqualTo("Margherita");
        assertThat(iris(individuals.get("individuals").get("pageElements")))
                .containsExactly(PIZZA + "ExampleMargherita");
        var page = json(send(HttpMethod.GET, "/individuals/page-containing?iri="
                + encode(PIZZA + "ExampleMargherita"), VIEWER, null));
        assertThat(page.get("individual").get("displayName").asText()).isNotBlank();
        assertThat(iris(page.get("individuals").get("pageElements"))).contains(PIZZA + "ExampleMargherita");
        assertThat(page.get("mode").asText()).isIn("ALL", "DIRECT");

        var individualFrame = json(send(HttpMethod.GET, "/frames/individual?iri="
                + encode(PIZZA + "ExampleMargherita"), VIEWER, null));
        assertThat(individualFrame.get("rendered").get("kind").asText()).isEqualTo("NamedIndividualFrame");
        assertThat(json(send(HttpMethod.GET, "/frames/object-property?iri=" + encode(PIZZA + "hasTopping"),
                             VIEWER, null)).get("rendered").get("kind").asText()).isEqualTo("ObjectPropertyFrame");
        var manchester = json(send(HttpMethod.GET, "/frames/manchester?iri=" + margherita + "&type=Class", VIEWER,
                                   null));
        assertThat(manchester.get("frameText").asText()).startsWith("Class: Margherita");
        var completions = objectMapper.createObjectNode().put("text", "Class: Margherita\n    SubClassOf: Mozz")
                                      .put("cursor", 37);
        assertThat(json(send(HttpMethod.POST, "/frames/manchester/completions", VIEWER, completions))
                           .get("choices").toString()).contains("Mozzarella Topping");

        var ontologies = json(send(HttpMethod.GET, "/ontologies", VIEWER, null));
        assertThat(ontologies.get(0).get("root").asBoolean()).isTrue();
        assertThat(ontologies.get(0).get("ontologyIri").asText()).isEqualTo(PizzaOntology.ONTOLOGY_IRI.toString());
        var annotations = json(send(HttpMethod.GET, "/ontologies/annotations", VIEWER, null));
        assertThat(annotations.get("annotations")).hasSize(PizzaOntology.ONTOLOGY_ANNOTATION_COUNT);
        var edit = objectMapper.createObjectNode();
        edit.set("from", annotations.get("annotations"));
        var to = (ArrayNode) annotations.get("annotations").deepCopy();
        to.add(objectMapper.createObjectNode()
                           .put("type", "PropertyAnnotationValue")
                           .<ObjectNode>set("property", objectMapper.createObjectNode()
                                                                    .put("type", "owl:AnnotationProperty")
                                                                    .put("iri", RDFS_COMMENT))
                           .set("value", objectMapper.createObjectNode().put("lang", "en").put("value", "Edited")));
        edit.set("to", to);
        var edited = json(send(HttpMethod.PUT, "/ontologies/annotations?ontologyIri="
                + encode(PizzaOntology.ONTOLOGY_IRI.toString()), EDITOR, edit));
        assertThat(edited.get("result").get("annotations")).hasSize(PizzaOntology.ONTOLOGY_ANNOTATION_COUNT + 1);
    }

    @Test
    void badParametersShouldBeRefused() {
        var notAnIri = send(HttpMethod.GET, "/frames/class?iri=" + encode("not an iri"), VIEWER, null);
        assertThat(notAnIri.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(notAnIri).get("code").asText()).isEqualTo("INVALID_IRI");
        var relative = send(HttpMethod.GET, "/frames/class?iri=Margherita", VIEWER, null);
        assertThat(relative.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(relative).get("code").asText()).isEqualTo("INVALID_IRI");
        var reserved = send(HttpMethod.GET, "/frames/class?iri=" + encode("entity:Pizza"), VIEWER, null);
        assertThat(reserved.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(reserved).get("code").asText()).isEqualTo("INVALID_IRI");
        assertThat(send(HttpMethod.GET, "/frames/datatype?iri=" + encode(PIZZA + "X"), VIEWER, null)
                           .getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(send(HttpMethod.GET, "/hierarchies/Datatype/roots", VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(send(HttpMethod.GET, "/entities/rendering?iri=" + encode(PIZZA + "X") + "&type=Thing", VIEWER,
                        null).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(send(HttpMethod.GET, "/entities/deprecated?page=0", VIEWER, null).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        var unknownProject = exchangeUri(HttpMethod.GET, "/api/v1/projects/00000000-0000-0000-0000-000000000000"
                + "/hierarchies/Class/roots", bearer(VIEWER), null, null);
        assertThat(unknownProject.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(send(HttpMethod.GET, "/ontologies", "admin", null).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        var funghi = json(send(HttpMethod.GET, "/frames/class?iri=" + encode(PIZZA + "Funghi"), EDITOR, null))
                .get("frame");
        var otherEntity = send(HttpMethod.PUT, "/frames/class?iri=" + encode(PIZZA + "Margherita"), EDITOR,
                               update(funghi, withComment(funghi, "Mushrooms"), null));
        assertThat(otherEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // A larger document is refused: one sent in chunks once the limit has been read, one that declares its
        // size before anything is read
        var tooLarge = postInChunks("/axioms", "#".repeat(OntologiesController.MAX_DOCUMENT_BYTES + 1));
        assertThat(tooLarge.statusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE.value());
        assertThat(tooLarge.body()).contains("\"code\":\"PAYLOAD_TOO_LARGE\"");
        assertThat(responseToAHeadDeclaring("/axioms", OntologiesController.MAX_DOCUMENT_BYTES + 1L))
                .startsWith("HTTP/1.1 413")
                .contains("\"code\":\"PAYLOAD_TOO_LARGE\"");
    }

    /**
     * Posts the Turtle document without a {@code Content-Length}, so that only the server's reading limits it.
     */
    private HttpResponse<String> postInChunks(String path, String document) {
        var request = HttpRequest.newBuilder(URI.create(rest.getRootUri() + projectPath + path))
                                 .header(HttpHeaders.AUTHORIZATION, bearer(EDITOR))
                                 .header(HttpHeaders.CONTENT_TYPE, "text/turtle")
                                 .POST(HttpRequest.BodyPublishers.fromPublisher(
                                         HttpRequest.BodyPublishers.ofString(document)))
                                 .build();
        try {
            return HttpClient.newBuilder()
                             .version(HttpClient.Version.HTTP_1_1)
                             .build()
                             .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Sends only the head of a Turtle POST that declares a body of the length, and reads the response until its
     * problem code: the server must answer without the body. Blocking clients that send the whole body first cannot
     * show this, as the server closes the connection once it has discarded 2 MB of the unread body.
     */
    private String responseToAHeadDeclaring(String path, long contentLength) {
        var root = URI.create(rest.getRootUri());
        try (var socket = new Socket(root.getHost(), root.getPort())) {
            socket.setSoTimeout(10_000);
            var head = "POST " + projectPath + path + " HTTP/1.1\r\n"
                    + "Host: " + root.getHost() + ":" + root.getPort() + "\r\n"
                    + "Authorization: " + bearer(EDITOR) + "\r\n"
                    + "Content-Type: text/turtle\r\n"
                    + "Content-Length: " + contentLength + "\r\n\r\n";
            socket.getOutputStream().write(head.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            var response = new StringBuilder();
            var buffer = new byte[4096];
            int read;
            while (!response.toString().contains("PAYLOAD_TOO_LARGE")
                    && (read = socket.getInputStream().read(buffer)) >= 0) {
                response.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            }
            return response.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> childrenOfPizza() {
        return iris(json(send(HttpMethod.GET, "/hierarchies/Class/children?iri=" + encode(PIZZA + "Pizza"),
                              VIEWER, null)).get("children").get("pageElements"));
    }

    private ObjectNode create(String sourceText, String parentIri) {
        var request = objectMapper.createObjectNode()
                                  .put("entityType", "Class")
                                  .put("sourceText", sourceText)
                                  .put("langTag", "en");
        request.putArray("parents").addObject().put("type", "owl:Class").put("iri", parentIri);
        return request;
    }

    private ObjectNode update(JsonNode from, JsonNode to, @Nullable String commitMessage) {
        var request = objectMapper.createObjectNode();
        request.set("from", from);
        request.set("to", to);
        if (commitMessage != null) {
            request.put("commitMessage", commitMessage);
        }
        return request;
    }

    /**
     * The plain frame with an rdfs:comment in English.
     */
    private JsonNode withComment(JsonNode frame, String comment) {
        var to = (ObjectNode) frame.deepCopy();
        var values = (ArrayNode) to.get("propertyValues");
        var value = values.addObject().put("type", "PropertyAnnotationValue");
        value.putObject("property").put("type", "owl:AnnotationProperty").put("iri", RDFS_COMMENT);
        value.putObject("value").put("lang", "en").put("value", comment);
        return to;
    }

    private static List<String> iris(Iterable<JsonNode> nodes) {
        var iris = new ArrayList<String>();
        nodes.forEach(node -> iris.add(node.get("entity").get("iri").asText()));
        return iris;
    }

    private ResponseEntity<String> send(HttpMethod method, String path, String user, @Nullable Object body) {
        return exchangeUri(method, projectPath + path, bearer(user), body, body == null ? null
                                                                                        : MediaType.APPLICATION_JSON);
    }

    private ResponseEntity<String> post(String path, String contentType, String document) {
        return exchangeUri(HttpMethod.POST, projectPath + path, bearer(EDITOR), document,
                           MediaType.parseMediaType(contentType));
    }

    /**
     * Sends the path as it is: IRIs in the query are encoded already, and must not be encoded again.
     */
    private ResponseEntity<String> exchangeUri(HttpMethod method,
                                               String path,
                                               String authorization,
                                               @Nullable Object body,
                                               @Nullable MediaType contentType) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        if (contentType != null) {
            headers.setContentType(contentType);
        }
        return rest.exchange(URI.create(rest.getRootUri() + path), method, new HttpEntity<>(body, headers),
                             String.class);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
