package org.industrial.ontology.app.frame;

import java.util.concurrent.atomic.AtomicBoolean;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.AxiomsService.Syntax;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link OntologyService} and {@link AxiomsService} (07 5.1-1, 5.1-14) on the pizza project.
 */
class OntologyServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final String PIZZA = PizzaOntology.NAMESPACE;

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectRegistry registry;

    private ProjectId projectId;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory);
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        var fixture = new ProjectTestFixture(context);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Nested
    class Ontologies {

        private OntologyService ontologies;

        @BeforeEach
        void setUp() {
            ontologies = context.bean(OntologyService.class);
        }

        @Test
        void theProjectShouldHaveItsRootOntology() {
            var list = ontologies.getOntologies(BOB, projectId);
            assertThat(list).hasSize(1);
            assertThat(list.get(0).root()).isTrue();
            assertThat(list.get(0).ontologyId().getOntologyIRI().toJavaUtil()).contains(PizzaOntology.ONTOLOGY_IRI);
            assertThat(list.get(0).shortForm()).isNotBlank();
        }

        @Test
        void theAnnotationsShouldBePlainAndRenderedInTheSameOrder() {
            var annotations = ontologies.getAnnotations(BOB, projectId, null);
            assertThat(annotations.annotations()).hasSize(PizzaOntology.ONTOLOGY_ANNOTATION_COUNT);
            assertThat(annotations.rendered()).hasSize(PizzaOntology.ONTOLOGY_ANNOTATION_COUNT);
            for (int i = 0; i < annotations.annotations().size(); i++) {
                assertThat(annotations.rendered().get(i).getProperty().getEntity())
                        .isEqualTo(annotations.annotations().get(i).getProperty());
            }
            assertThat(ontologies.getAnnotations(BOB, projectId, PizzaOntology.ONTOLOGY_IRI))
                    .isEqualTo(annotations);
            assertThatThrownBy(() -> ontologies.getAnnotations(BOB, projectId, IRI.create("http://example.org/o")))
                    .isInstanceOf(WpException.class)
                    .satisfies(e -> {
                        assertThat(((WpException) e).getCode()).isEqualTo(OntologyService.ONTOLOGY_NOT_FOUND);
                        assertThat(((WpException) e).getStatus()).isEqualTo(404);
                    });
        }

        @Test
        void theAnnotationsShouldBeReplacedInOneRevision() {
            var from = ontologies.getAnnotations(ALICE, projectId, null).annotations();
            var to = new ArrayList<>(from.subList(1, from.size()));
            var comment = PlainPropertyAnnotationValue.get(DATA_FACTORY.getRDFSComment(),
                                                           DATA_FACTORY.getOWLLiteral("Edited", "en"));
            to.add(comment);

            var outcome = ontologies.setAnnotations(ALICE, projectId, null, from, to, null);

            var revision = outcome.revision().orElseThrow();
            assertThat(revision.getHighLevelDescription()).isEqualTo("Edited ontology annotations");
            assertThat(revision.getChanges()).hasSize(2);
            assertThat(outcome.result().annotations()).containsExactlyInAnyOrderElementsOf(to);
            assertThat(ontologies.getAnnotations(ALICE, projectId, null).annotations())
                    .containsExactlyInAnyOrderElementsOf(to);
        }

        @Test
        void editingAnnotationsShouldNeedTheEditActions() {
            var from = ontologies.getAnnotations(BOB, projectId, null).annotations();
            assertThatThrownBy(() -> ontologies.setAnnotations(BOB, projectId, null, from, List.of(), null))
                    .isInstanceOf(PermissionDeniedException.class);
            assertThatThrownBy(() -> ontologies.getOntologies(UserId.getUserId("carol"), projectId))
                    .isInstanceOf(PermissionDeniedException.class);
        }
    }

    @Nested
    class Axioms {

        private AxiomsService axioms;

        @BeforeEach
        void setUp() {
            axioms = context.bean(AxiomsService.class);
        }

        @Test
        void axiomsShouldBeAddedInEachSyntax() {
            var turtle = """
                    @prefix owl: <http://www.w3.org/2002/07/owl#> .
                    @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
                    <%1$sCalzone> a owl:Class ; rdfs:subClassOf <%1$sPizza> .
                    """.formatted(PIZZA);
            var outcome = axioms.addAxioms(ALICE, projectId, turtle, Syntax.TURTLE, "Calzones");
            assertThat(outcome.result()).isEqualTo(2);
            assertThat(outcome.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Calzones");
            assertThat(parentsOf("Calzone")).contains(PizzaOntology.cls(DATA_FACTORY, "Pizza"));

            var rdfXml = """
                    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                             xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                             xmlns:owl="http://www.w3.org/2002/07/owl#">
                        <owl:Class rdf:about="%1$sStromboli">
                            <rdfs:subClassOf rdf:resource="%1$sPizza"/>
                        </owl:Class>
                    </rdf:RDF>
                    """.formatted(PIZZA);
            assertThat(axioms.addAxioms(ALICE, projectId, rdfXml, Syntax.RDF_XML, null).result()).isEqualTo(2);
            assertThat(parentsOf("Stromboli")).contains(PizzaOntology.cls(DATA_FACTORY, "Pizza"));

            var functional = """
                    Ontology(
                    SubClassOf(<%1$sStromboli> <%1$sNamedPizza>)
                    )
                    """.formatted(PIZZA);
            var added = axioms.addAxioms(ALICE, projectId, functional, Syntax.FUNCTIONAL, null);
            assertThat(added.result()).isEqualTo(1);
            assertThat(added.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Added axioms");
            assertThat(parentsOf("Stromboli")).contains(PizzaOntology.cls(DATA_FACTORY, "NamedPizza"));
        }

        @Test
        void axiomsThatAreThereAlreadyShouldChangeNothing() {
            var functional = "Ontology(SubClassOf(<%1$sMargherita> <%1$sNamedPizza>))".formatted(PIZZA);
            var outcome = axioms.addAxioms(ALICE, projectId, functional, Syntax.FUNCTIONAL, null);
            assertThat(outcome.result()).isZero();
            assertThat(outcome.revision()).isEmpty();
        }

        @Test
        void axiomsShouldBeDeleted() {
            var functional = "Ontology(SubClassOf(<%1$sMargherita> <%1$sNamedPizza>))".formatted(PIZZA);
            var outcome = axioms.deleteAxioms(ALICE, projectId, functional, Syntax.FUNCTIONAL, null);
            assertThat(outcome.result()).isEqualTo(1);
            assertThat(outcome.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Deleted axioms");
            assertThat(parentsOf("Margherita")).doesNotContain(PizzaOntology.cls(DATA_FACTORY, "NamedPizza"));
        }

        @Test
        void documentsThatDoNotParseInTheirSyntaxShouldBeRefused() {
            var functional = "Ontology(SubClassOf(<%1$sMargherita> <%1$sNamedPizza>))".formatted(PIZZA);
            for (var syntax : List.of(Syntax.TURTLE, Syntax.RDF_XML)) {
                assertThatThrownBy(() -> axioms.addAxioms(ALICE, projectId, functional, syntax, null))
                        .isInstanceOf(WpException.class)
                        .satisfies(e -> {
                            assertThat(((WpException) e).getCode()).isEqualTo(AxiomsService.UNPARSABLE_AXIOMS);
                            assertThat(((WpException) e).getStatus()).isEqualTo(400);
                        });
            }
        }

        @Test
        void theImportsOfTheDocumentShouldNotBeFetched(@TempDir Path elsewhere) throws IOException {
            var secret = """
                    @prefix owl: <http://www.w3.org/2002/07/owl#> .
                    <http://example.org/secret> a owl:Ontology .
                    <http://example.org/secret#Secret> a owl:Class .
                    """;
            var imported = elsewhere.resolve("secret.ttl");
            Files.writeString(imported, secret);
            var requests = new AtomicInteger();
            var server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            server.createContext("/", exchange -> {
                requests.incrementAndGet();
                var body = secret.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/turtle");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
            try {
                var remote = "http://localhost:" + server.getAddress().getPort() + "/secret.ttl";
                var turtle = """
                        @prefix owl: <http://www.w3.org/2002/07/owl#> .
                        <http://example.org/posted> a owl:Ontology ; owl:imports <%s>, <%s> .
                        <%sCalzone> a owl:Class .
                        """.formatted(imported.toUri(), remote, PIZZA);

                assertThat(axioms.addAxioms(ALICE, projectId, turtle, Syntax.TURTLE, null).result()).isEqualTo(1);

                assertThat(requests).hasValue(0);
                var signature = registry.get(projectId).indexes().get(ProjectSignatureIndex.class).getSignature()
                                        .toList();
                assertThat(signature).contains(PizzaOntology.cls(DATA_FACTORY, "Calzone"))
                                     .doesNotContain(DATA_FACTORY.getOWLClass(
                                             IRI.create("http://example.org/secret#Secret")));
            } finally {
                server.stop(0);
            }
        }

        @Test
        void reservedIrisShouldBeRefusedAndDocumentsOnlyReadForEditors() {
            var fresh = "Ontology(Declaration(Class(<entity:Calzone>)))";
            assertThatThrownBy(() -> axioms.addAxioms(ALICE, projectId, fresh, Syntax.FUNCTIONAL, null))
                    .isInstanceOf(WpException.class)
                    .extracting("code").isEqualTo(AxiomsService.UNPARSABLE_AXIOMS);
            var read = new AtomicBoolean();
            assertThatThrownBy(() -> axioms.addAxioms(BOB, projectId, () -> {
                read.set(true);
                return fresh;
            }, Syntax.FUNCTIONAL, null))
                    .isInstanceOf(PermissionDeniedException.class);
            assertThat(read).isFalse();
        }

        @Test
        void changingAxiomsShouldNeedEditOntology() {
            var functional = "Ontology(SubClassOf(<%1$sMargherita> <%1$sNamedPizza>))".formatted(PIZZA);
            assertThatThrownBy(() -> axioms.deleteAxioms(BOB, projectId, functional, Syntax.FUNCTIONAL, null))
                    .isInstanceOf(PermissionDeniedException.class);
            assertThatThrownBy(() -> axioms.addAxioms(BOB, projectId, functional, Syntax.FUNCTIONAL, null))
                    .isInstanceOf(PermissionDeniedException.class);
        }
    }

    private List<OWLClass> parentsOf(String localName) {
        var ontologyId = registry.get(projectId).defaultOntologyIdManager().getDefaultOntologyId();
        return registry.get(projectId)
                       .indexes()
                       .get(SubClassOfAxiomsBySubClassIndex.class)
                       .getSubClassOfAxiomsForSubClass(PizzaOntology.cls(DATA_FACTORY, localName), ontologyId)
                       .filter(axiom -> !axiom.getSuperClass().isAnonymous())
                       .map(axiom -> axiom.getSuperClass().asOWLClass())
                       .toList();
    }
}
