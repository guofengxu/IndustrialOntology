package org.industrial.ontology.api.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.SharingService;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.sharing.SharingPermission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_CREATOR;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_UPLOADER;

/**
 * The project endpoints of docs/02 §3 (S6, 05 P1-04): the S6 completion flow over HTTP, from creating projects, empty
 * and from an upload, to the trash and back, then downloads and the error responses.
 */
class ProjectsControllerIT extends ApiIntegrationTest {

    /**
     * A small menu ontology with labels in two languages.
     */
    private static final String MENU = """
            @prefix : <http://www.industrial-ontology.org/test/menu#> .
            @prefix owl: <http://www.w3.org/2002/07/owl#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
            <http://www.industrial-ontology.org/test/menu> a owl:Ontology .
            :Pizza a owl:Class ; rdfs:label "Pizza"@en , "比萨"@zh .
            :Margherita a owl:Class ; rdfs:subClassOf :Pizza ; rdfs:label "Margherita"@en .
            """;

    @Autowired
    private SharingService sharingService;

    @Test
    void projectsShouldBeCreatedListedSetUpSharedTrashedAndRestored() {
        grantApplicationRoles("editor", PROJECT_CREATOR, PROJECT_UPLOADER);
        get("/api/v1/me", bearer("viewer"));

        // Create: empty, and from an upload
        var empty = exchange(HttpMethod.POST, "/api/v1/projects", bearer("editor"),
                             Map.of("displayName", "Empty menu", "description", "Nothing yet", "language", "en"));
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var emptyId = json(empty).get("projectId").asText();
        assertThat(empty.getHeaders().getLocation()).hasPath("/api/v1/projects/" + emptyId);
        assertThat(json(empty).get("owner").asText()).isEqualTo("editor");
        assertThat(json(empty).get("description").asText()).isEqualTo("Nothing yet");
        assertThat(json(empty).get("defaultLanguage").get("lang")).as(empty.getBody()).isNotNull();
        assertThat(json(empty).get("defaultLanguage").get("lang").asText()).isEqualTo("en");

        var upload = upload(bearer("editor"), "menu.ttl", MENU.getBytes(StandardCharsets.UTF_8));
        assertThat(upload.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(json(upload).get("fileName").asText()).isEqualTo("menu.ttl");
        assertThat(json(upload).get("size").asLong()).isPositive();
        var created = exchange(HttpMethod.POST, "/api/v1/projects", bearer("editor"),
                               Map.of("displayName", "Menu", "language", "en",
                                      "sourceDocumentId", json(upload).get("documentId").asText()));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var projectId = json(created).get("projectId").asText();
        var project = "/api/v1/projects/" + projectId;

        // The three lists
        assertThat(projectIds("owned", "editor")).contains(emptyId, projectId);
        assertThat(json(get("/api/v1/projects?filter=owned", bearer("editor"))))
                .filteredOn(item -> item.get("projectId").asText().equals(projectId))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.get("downloadable").asBoolean()).isTrue();
                    assertThat(item.get("trashable").asBoolean()).isTrue();
                    assertThat(item.get("displayName").asText()).isEqualTo("Menu");
                });
        assertThat(projectIds("shared", "viewer")).doesNotContain(projectId);
        assertThat(projectIds("trash", "editor")).doesNotContain(projectId);

        // Settings, prefixes and languages
        var settings = (ObjectNode) json(get(project + "/settings", bearer("editor")));
        settings.put("description", "Pizzas on the menu");
        settings.putArray("webhooks").addObject().put("payloadUrl", "https://hooks.example.org/menu")
                .putArray("eventTypes").add("PROJECT_CHANGED");
        var setSettings = exchange(HttpMethod.PUT, project + "/settings", bearer("editor"), settings);
        assertThat(setSettings.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(setSettings).get("description").asText()).isEqualTo("Pizzas on the menu");
        assertThat(json(get(project + "/webhooks", bearer("editor"))).get(0).get("payloadUrl").asText())
                .isEqualTo("https://hooks.example.org/menu");
        var prefixes = exchange(HttpMethod.PUT, project + "/prefixes", bearer("editor"),
                                Map.of("prefixes", Map.of("menu:", "http://www.industrial-ontology.org/test/menu#")));
        assertThat(json(prefixes).get("prefixes").get("menu:").asText())
                .isEqualTo("http://www.industrial-ontology.org/test/menu#");
        assertThat(json(get(project + "/lang-tags", bearer("editor")))).extracting(JsonNode::asText)
                                                                       .containsExactly("en", "zh");
        assertThat(json(get(project + "/languages", bearer("editor"))).get("languageUsage"))
                .extracting(usage -> usage.get("language").path("lang").asText())
                .contains("en", "zh");

        // Sharing
        var sharing = exchange(HttpMethod.PUT, project + "/sharing", bearer("editor"),
                               Map.of("sharingSettings", List.of(Map.of("userId", "editor", "permission", "MANAGE"),
                                                                 Map.of("userId", "viewer", "permission", "VIEW")),
                                      "linkSharing", "NONE"));
        assertThat(sharing.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(sharing).get("sharingSettings")).extracting(setting -> setting.get("userId").asText())
                                                        .containsExactly("editor", "viewer");
        assertThat(json(sharing).get("linkSharing").asText()).isEqualTo("NONE");
        assertThat(projectIds("shared", "viewer")).contains(projectId);
        assertThat(get(project, bearer("viewer")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(get(project + "/permissions", bearer("viewer")))).extracting(JsonNode::asText)
                                                                         .contains("ViewProject")
                                                                         .doesNotContain("EditOntology");
        assertForbidden(exchange(HttpMethod.PUT, project + "/settings", bearer("viewer"), settings));
        assertForbidden(get(project + "/sharing", bearer("viewer")));
        assertForbidden(exchange(HttpMethod.POST, project + "/trash", bearer("viewer"), null));

        // The trash and back
        var trashed = exchange(HttpMethod.POST, project + "/trash", bearer("editor"), null);
        assertThat(json(trashed).get("inTrash").asBoolean()).isTrue();
        assertThat(projectIds("owned", "editor")).doesNotContain(projectId);
        assertThat(projectIds("trash", "editor")).contains(projectId);
        assertThat(projectIds("shared", "viewer")).doesNotContain(projectId);
        var restored = exchange(HttpMethod.DELETE, project + "/trash", bearer("editor"), null);
        assertThat(json(restored).get("inTrash").asBoolean()).isFalse();
        assertThat(projectIds("owned", "editor")).contains(projectId);
        assertThat(projectIds("trash", "editor")).doesNotContain(projectId);
    }

    @Test
    void everyFormatShouldBeDownloadable() throws IOException {
        var projectId = createMenuProject();

        for (var format : List.of("owl", "ttl", "owx", "omn", "ofn")) {
            var response = rest.exchange("/api/v1/projects/" + projectId + "/download?format=" + format,
                                         HttpMethod.GET, new HttpEntity<>(headers(bearer("editor"))), byte[].class);
            assertThat(response.getStatusCode()).as(format).isEqualTo(HttpStatus.OK);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("application/zip"));
            assertThat(response.getHeaders().getContentDisposition().getFilename())
                    .isEqualTo("menu-ontologies." + format + ".zip");
            assertThat(zipEntries(response.getBody())).singleElement()
                                                      .satisfies(entry -> assertThat(entry).endsWith("." + format));
        }
    }

    /**
     * The legacy {@code FileDownloadParametersTestCase}: on {@code /download} a malformed revision means the head
     * revision and an unknown format RDF/XML; the new path refuses both.
     */
    @Test
    void theLegacyDownloadParametersShouldStayLenient() throws IOException {
        var projectId = createMenuProject();

        var legacy = rest.exchange("/download?project=" + projectId + "&revision=x&format=junk", HttpMethod.GET,
                                   new HttpEntity<>(headers(bearer("editor"))), byte[].class);
        assertThat(legacy.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(zipEntries(legacy.getBody())).singleElement()
                                                .satisfies(entry -> assertThat(entry).endsWith(".owl"));
        assertThat(rest.exchange("/download?project=" + projectId + "&revision=1&format=ttl", HttpMethod.GET,
                                 new HttpEntity<>(headers(bearer("editor"))), byte[].class)
                       .getHeaders().getContentDisposition().getFilename())
                .isEqualTo("menu-revision-1-ontologies.ttl.zip");

        var download = "/api/v1/projects/" + projectId + "/download";
        assertProblem(get(download + "?format=junk", bearer("editor")), HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        assertProblem(get(download + "?revision=x", bearer("editor")), HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        assertProblem(get(download + "?revision=7", bearer("editor")), HttpStatus.NOT_FOUND, "REVISION_NOT_FOUND");
        assertProblem(get("/download?project=x", bearer("editor")), HttpStatus.BAD_REQUEST, "INVALID_REQUEST");
        assertProblem(get(download, bearer("viewer")), HttpStatus.FORBIDDEN, "PERMISSION_DENIED");
    }

    @Test
    void badAndUnknownProjectIdsShouldBeTold() {
        assertProblem(get("/api/v1/projects/not-a-uuid", bearer("editor")), HttpStatus.BAD_REQUEST,
                      "INVALID_REQUEST");
        assertProblem(get("/api/v1/projects/" + UUID.randomUUID(), bearer("editor")), HttpStatus.NOT_FOUND,
                      "PROJECT_NOT_FOUND");
        assertProblem(get("/api/v1/projects/" + UUID.randomUUID() + "/settings", bearer("admin")),
                      HttpStatus.NOT_FOUND, "PROJECT_NOT_FOUND");
        assertProblem(get("/api/v1/projects?filter=everything", bearer("editor")), HttpStatus.BAD_REQUEST,
                      "INVALID_REQUEST");
        assertProblem(exchange(HttpMethod.POST, "/api/v1/projects", bearer("viewer"), Map.of("displayName", "No")),
                      HttpStatus.FORBIDDEN, "PERMISSION_DENIED");
    }

    @Test
    void sharingWithAnUnknownUserShouldBeRefused() {
        var projectId = createProject("editor", "Unknown users").getId();

        var response = exchange(HttpMethod.PUT, "/api/v1/projects/" + projectId + "/sharing", bearer("editor"),
                                Map.of("sharingSettings",
                                       List.of(Map.of("userId", "editor", "permission", "MANAGE"),
                                               Map.of("userId", "somebody-unknown", "permission", "EDIT"))));

        assertProblem(response, HttpStatus.BAD_REQUEST, "USER_NOT_FOUND");
        assertThat(json(response).get("detail").asText()).contains("somebody-unknown");
    }

    /**
     * {@code wp-cli set-permissions} calls {@link SharingService#setUserPermission}; the server does not cache
     * permissions, so the next request sees the role (S6 completion criterion).
     */
    @Test
    void aRoleSetFromTheCommandLineShouldApplyToTheNextRequest() {
        var projectId = createProject("editor", "Set permissions");
        var permissions = "/api/v1/projects/" + projectId.getId() + "/permissions";
        assertThat(json(get(permissions, bearer("viewer")))).extracting(JsonNode::asText)
                                                            .doesNotContain("EditOntology");

        sharingService.setUserPermission(projectId, UserId.getUserId("viewer"), SharingPermission.EDIT);

        assertThat(json(get(permissions, bearer("viewer")))).extracting(JsonNode::asText).contains("EditOntology");
    }

    @Test
    void openingAProjectShouldMakeItRecent() {
        var projectId = createProject("editor", "Recently opened");

        var opened = exchange(HttpMethod.POST, "/api/v1/projects/" + projectId.getId() + "/open", bearer("editor"),
                              null);

        assertThat(opened.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(get("/api/v1/projects", bearer("editor"))))
                .filteredOn(item -> item.get("projectId").asText().equals(projectId.getId()))
                .singleElement()
                .satisfies(item -> assertThat(item.get("lastOpenedAt").isTextual()).isTrue());
    }

    @Test
    void newEntitySettingsShouldRoundTripThroughJson() {
        var projectId = createProject("editor", "New entities").getId();
        var crudSettings = "/api/v1/projects/" + projectId + "/crud-settings";

        var defaults = json(get(crudSettings, bearer("editor")));
        assertThat(defaults.get("suffixSettings").get("_class").asText()).isEqualTo("Uuid");

        var suppliedNames = Map.of("prefixSettings", Map.of("iriPrefix", "http://www.industrial-ontology.org/m#",
                                                            "conditionalIriPrefixes", List.of()),
                                   "suffixSettings", Map.of("_class", "SuppliedName",
                                                            "whiteSpaceTreatment", "TRANSFORM_TO_CAMEL_CASE"));
        var stored = exchange(HttpMethod.PUT, crudSettings, bearer("editor"), suppliedNames);
        assertThat(stored.getStatusCode()).as(stored.getBody()).isEqualTo(HttpStatus.OK);
        assertThat(json(get(crudSettings, bearer("editor")))).isEqualTo(json(stored));
        assertThat(json(stored).get("suffixSettings").get("_class").asText()).isEqualTo("SuppliedName");
        assertThat(json(stored).get("prefixSettings").get("iriPrefix").asText())
                .isEqualTo("http://www.industrial-ontology.org/m#");
        assertThat(json(get("/api/v1/crud-kits", bearer("viewer")))).extracting(kit -> kit.get("kitId").asText())
                                                                    .containsExactly("UUID", "OBO",
                                                                                     "SuppliedNameSuffix");
    }

    @Test
    void administratorsShouldRebuildThePermissions() {
        assertProblem(exchange(HttpMethod.POST, "/api/v1/admin/permissions/rebuild", bearer("viewer"), null),
                      HttpStatus.FORBIDDEN, "PERMISSION_DENIED");
        assertThat(exchange(HttpMethod.POST, "/api/v1/admin/permissions/rebuild", bearer("admin"), null)
                           .getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private void grantApplicationRoles(String userName, BuiltInRole... roles) {
        accessManager.setAssignedRoles(Subject.forUser(userName), ApplicationResource.get(),
                                       List.of(roles).stream().map(BuiltInRole::getRoleId).toList());
    }

    /**
     * Creates a project from an upload of the menu ontology and returns its id.
     */
    private String createMenuProject() {
        grantApplicationRoles("editor", PROJECT_CREATOR, PROJECT_UPLOADER);
        var upload = upload(bearer("editor"), "menu.ttl", MENU.getBytes(StandardCharsets.UTF_8));
        var created = exchange(HttpMethod.POST, "/api/v1/projects", bearer("editor"),
                               Map.of("displayName", "Menu", "language", "en",
                                      "sourceDocumentId", json(upload).get("documentId").asText()));
        return json(created).get("projectId").asText();
    }

    private List<String> projectIds(String filter, String userName) {
        return StreamSupport.stream(json(get("/api/v1/projects?filter=" + filter, bearer(userName))).spliterator(),
                                    false)
                            .map(item -> item.get("projectId").asText())
                            .toList();
    }

    private ResponseEntity<String> upload(String authorization, String fileName, byte[] content) {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });
        var headers = headers(authorization);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return rest.exchange("/api/v1/uploads", HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
    }

    private static HttpHeaders headers(String authorization) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        return headers;
    }

    private static List<String> zipEntries(byte[] zip) throws IOException {
        var names = new ArrayList<String>();
        try (var in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (var entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        return names;
    }

    private void assertForbidden(ResponseEntity<String> response) {
        assertProblem(response, HttpStatus.FORBIDDEN, "PERMISSION_DENIED");
    }

    private void assertProblem(ResponseEntity<String> response, HttpStatus status, String code) {
        assertThat(response.getStatusCode()).as(response.getBody()).isEqualTo(status);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(json(response).get("code").asText()).isEqualTo(code);
    }
}
