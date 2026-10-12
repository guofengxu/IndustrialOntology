package org.industrial.ontology.app.entity;

import org.industrial.ontology.app.frame.AxiomsService;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.CommentId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.SearchResultMatchPosition;
import org.industrial.ontology.domain.usage.UsageFilter;
import org.industrial.ontology.domain.usage.UsageReferenceComparator;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_COMMENT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link EntityService} and {@link EntityRenderingService} (07 5.1-2) on the pizza project.
 */
class EntityServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final UserId CAROL = UserId.getUserId("carol");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLClass PIZZA = PizzaOntology.cls(DATA_FACTORY, "Pizza");

    private static final OWLClass FUNGHI = PizzaOntology.cls(DATA_FACTORY, "Funghi");

    private static final OWLClass MARGHERITA = PizzaOntology.cls(DATA_FACTORY, "Margherita");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private EntityService entities;

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
        entities = context.bean(EntityService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
        fixture.grantProjectRoles(CAROL, projectId, CAN_COMMENT);
    }

    @Test
    void eachLineShouldCreateAClassBelowTheParentInOneRevision() {
        var outcome = entities.createEntities(ALICE, projectId, EntityType.CLASS, "Stromboli\nCalzone", "en",
                                              Set.of(PIZZA), null);

        assertThat(outcome.result()).extracting(EntityNode::getBrowserText).containsExactly("Calzone", "Stromboli");
        assertThat(outcome.revision()).isPresent();
        var created = outcome.result().stream().map(EntityNode::getEntity).toList();
        assertThat(registry.get(projectId).hierarchies().classHierarchy().getChildren(PIZZA))
                .containsAll(created.stream().map(OWLEntity::asOWLClass).toList());
    }

    @Test
    void propertiesAndIndividualsShouldBeCreatedToo() {
        var hasIngredient = PizzaOntology.objectProperty(DATA_FACTORY, "hasIngredient");
        var objectProperty = entities.createEntities(ALICE, projectId, EntityType.OBJECT_PROPERTY, "hasSauce", "",
                                                     Set.of(hasIngredient), null).result().get(0).getEntity();
        assertThat(registry.get(projectId).hierarchies().objectPropertyHierarchy().getParents(
                objectProperty.asOWLObjectProperty())).contains(hasIngredient);
        assertThat(entities.createEntities(ALICE, projectId, EntityType.DATA_PROPERTY, "hasWeight", "", Set.of(),
                                           null).result()).hasSize(1);
        assertThat(entities.createEntities(ALICE, projectId, EntityType.ANNOTATION_PROPERTY, "source", "",
                                           Set.of(), null).result()).hasSize(1);
        var individual = entities.createEntities(ALICE, projectId, EntityType.NAMED_INDIVIDUAL, "TonightsPizza",
                                                 "", Set.of(MARGHERITA), "Dinner").result().get(0).getEntity();
        assertThat(registry.get(projectId).indexes().get(ClassAssertionAxiomsByIndividualIndex.class)
                           .getClassAssertionAxioms(individual.asOWLNamedIndividual(),
                                                    registry.get(projectId).defaultOntologyIdManager()
                                                            .getDefaultOntologyId())
                           .map(axiom -> axiom.getClassExpression())
                           .toList()).contains(MARGHERITA);
    }

    @Test
    void creatingShouldNeedTheCreateActionOfTheTypeAndValidInput() {
        assertThatThrownBy(() -> entities.createEntities(CAROL, projectId, EntityType.CLASS, "X", "", Set.of(),
                                                         null))
                .isInstanceOf(PermissionDeniedException.class);
        assertInvalid(() -> entities.createEntities(ALICE, projectId, EntityType.CLASS, " \n ", "", Set.of(),
                                                    null));
        assertInvalid(() -> entities.createEntities(ALICE, projectId, EntityType.CLASS, "X", "",
                                                    Set.of(PizzaOntology.objectProperty(DATA_FACTORY,
                                                                                        "hasTopping")), null));
        assertInvalid(() -> entities.createEntities(ALICE, projectId, EntityType.DATATYPE, "X", "", Set.of(),
                                                    null));
    }

    @Test
    void deletingShouldRemoveTheEntitiesAndTheirAxioms() {
        var outcome = entities.deleteEntities(ALICE, projectId, Set.of(FUNGHI));
        assertThat(outcome.result()).containsExactly(FUNGHI);
        assertThat(inSignature(FUNGHI)).isFalse();
        assertInvalid(() -> entities.deleteEntities(ALICE, projectId, Set.of(DATA_FACTORY.getOWLThing())));
        assertInvalid(() -> entities.deleteEntities(ALICE, projectId, Set.of()));
        assertThatThrownBy(() -> entities.deleteEntities(BOB, projectId, Set.of(MARGHERITA)))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void mergingShouldReplaceTheSourcesWithTheTarget() {
        var outcome = entities.mergeEntities(ALICE, projectId, Set.of(FUNGHI), MARGHERITA,
                                             MergedEntityTreatment.DELETE_MERGED_ENTITY, null);
        assertThat(outcome.result()).isEqualTo(MARGHERITA);
        assertThat(outcome.revision().orElseThrow().getHighLevelDescription())
                .isEqualTo("Merged " + FUNGHI.getIRI() + " into " + MARGHERITA.getIRI());
        assertThat(inSignature(FUNGHI)).isFalse();
        assertInvalid(() -> entities.mergeEntities(ALICE, projectId, Set.of(MARGHERITA), MARGHERITA,
                                                   MergedEntityTreatment.DELETE_MERGED_ENTITY, null));
        assertInvalid(() -> entities.mergeEntities(ALICE, projectId,
                                                   Set.of(PizzaOntology.objectProperty(DATA_FACTORY, "hasTopping")),
                                                   MARGHERITA, MergedEntityTreatment.DELETE_MERGED_ENTITY, null));
        assertThatThrownBy(() -> entities.mergeEntities(CAROL, projectId, Set.of(PIZZA), MARGHERITA,
                                                        MergedEntityTreatment.DELETE_MERGED_ENTITY, null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void aNewIriShouldRenameTheEntityEverywhereWithItsDiscussions() {
        var discussions = context.bean(DiscussionThreadRepository.class);
        discussions.saveThread(new EntityDiscussionThread(ThreadId.create(), projectId, FUNGHI, Status.OPEN,
                                                          ImmutableList.of(new Comment(CommentId.create(), ALICE,
                                                                                       1000L, Optional.empty(),
                                                                                       "Mushrooms?",
                                                                                       "<p>Mushrooms?</p>"))));
        var mushroom = PizzaOntology.cls(DATA_FACTORY, "Mushroom");

        var outcome = entities.changeEntityIri(ALICE, projectId, FUNGHI, mushroom.getIRI(), null);

        assertThat(outcome.result().before().getEntity()).isEqualTo(FUNGHI);
        assertThat(outcome.result().after().getEntity()).isEqualTo(mushroom);
        assertThat(outcome.revision().orElseThrow().getHighLevelDescription())
                .isEqualTo("Changed Class IRI from " + FUNGHI.getIRI() + " to " + mushroom.getIRI());
        assertThat(inSignature(FUNGHI)).isFalse();
        assertThat(inSignature(mushroom)).isTrue();
        assertThat(discussions.findThreads(projectId, mushroom)).hasSize(1);
        assertThat(discussions.findThreads(projectId, FUNGHI)).isEmpty();
    }

    @Test
    void aNewIriInUseOrAnUnknownEntityShouldBeRefused() {
        assertThatThrownBy(() -> entities.changeEntityIri(ALICE, projectId, FUNGHI, MARGHERITA.getIRI(), null))
                .isInstanceOf(WpException.class)
                .satisfies(e -> {
                    assertThat(((WpException) e).getCode()).isEqualTo(EntityService.IRI_IN_USE);
                    assertThat(((WpException) e).getStatus()).isEqualTo(409);
                });
        var unknown = PizzaOntology.cls(DATA_FACTORY, "Unknown");
        assertThatThrownBy(() -> entities.changeEntityIri(ALICE, projectId, unknown,
                                                          IRI.create(PizzaOntology.NAMESPACE + "Other"), null))
                .isInstanceOf(WpException.class)
                .satisfies(e -> assertThat(((WpException) e).getStatus()).isEqualTo(404));
        assertInvalid(() -> entities.changeEntityIri(ALICE, projectId, FUNGHI, FUNGHI.getIRI(), null));
        assertThat(inSignature(FUNGHI)).isTrue();
    }

    @Test
    void lookupShouldFindEntitiesByTheWordsOfTheirNames() {
        var matches = entities.lookupEntities(BOB, projectId, "mozz", Set.of(EntityType.CLASS), 10, null);
        assertThat(matches).isNotEmpty();
        var first = matches.get(0);
        assertThat(first.entity().getEntity()).isEqualTo(PizzaOntology.cls(DATA_FACTORY, "MozzarellaTopping"));
        assertThat(first.value()).isEqualTo("Mozzarella Topping");
        assertThat(first.positions()).containsExactly(SearchResultMatchPosition.get(0, 4));
        assertThat(entities.lookupEntities(BOB, projectId, "mozz", Set.of(EntityType.OBJECT_PROPERTY), 10, null))
                .isEmpty();
    }

    @Test
    void deprecatedEntitiesAndUsageShouldBeListed() {
        var deprecated = entities.getDeprecatedEntities(BOB, projectId, Set.of(EntityType.CLASS),
                                                        PageRequest.requestPageWithSize(1, 10));
        assertThat(deprecated.getPageElements()).extracting(data -> data.getEntity())
                                                .contains(PizzaOntology.cls(DATA_FACTORY, "Veneziana"));

        var mozzarella = PizzaOntology.cls(DATA_FACTORY, "MozzarellaTopping");
        var all = entities.getUsage(BOB, projectId, mozzarella, new UsageFilter(),
                                    PageRequest.requestPageWithSize(1, 100));
        assertThat(all.subject().getEntity()).isEqualTo(mozzarella);
        assertThat(all.axiomCount()).isGreaterThan(2);
        assertThat(all.references().getTotalElements()).isGreaterThan(2);
        assertThat(all.references().getPageElements()).isSortedAccordingTo(new UsageReferenceComparator(mozzarella));
        var firstTwo = entities.getUsage(BOB, projectId, mozzarella, new UsageFilter(),
                                         PageRequest.requestPageWithSize(1, 2));
        assertThat(firstTwo.references().getPageElements())
                .containsExactlyElementsOf(all.references().getPageElements().subList(0, 2));
    }

    /**
     * S7 security review: what authors write is escaped in the HTML description, which the legacy renderer copied
     * as it was.
     */
    @Test
    void theHtmlRenderingShouldEscapeWhatAuthorsWrote() {
        context.bean(AxiomsService.class).addAxioms(ALICE, projectId,
                "Ontology(AnnotationAssertion(<http://www.w3.org/2000/01/rdf-schema#comment> <%s> "
                        .formatted(MARGHERITA.getIRI()) + "\"<script>alert(1)</script>\"@en))",
                AxiomsService.Syntax.FUNCTIONAL, null);
        var html = context.bean(EntityRenderingService.class).getHtmlRendering(BOB, projectId, MARGHERITA).html();
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;").doesNotContain("<script>");
    }

    @Test
    void entitiesShouldNotBeRenamedOntoBuiltInOrReservedIris() {
        assertInvalid(() -> entities.changeEntityIri(ALICE, projectId, FUNGHI, DATA_FACTORY.getOWLThing().getIRI(),
                                                     null));
        assertInvalid(() -> entities.changeEntityIri(ALICE, projectId, FUNGHI, IRI.create("entity:Funghi"), null));
        assertThat(inSignature(FUNGHI)).isTrue();
    }

    @Test
    void renderingsShouldNeedViewProject() {
        var rendering = context.bean(EntityRenderingService.class);
        assertThat(rendering.getRendering(BOB, projectId, MARGHERITA).getBrowserText()).isEqualTo("Margherita");
        var html = rendering.getHtmlRendering(BOB, projectId, MARGHERITA);
        assertThat(html.entity().getEntity()).isEqualTo(MARGHERITA);
        assertThat(html.html()).contains("<span class=\"ms-section-kw\">SubClassOf:</span>")
                               .contains("Named&nbsp;Pizza");
        assertThatThrownBy(() -> rendering.getRendering(UserId.getUserId("dave"), projectId, MARGHERITA))
                .isInstanceOf(PermissionDeniedException.class);
        assertThat(rendering.getRendering(BOB, projectId, OWL2Datatype.XSD_STRING.getDatatype(DATA_FACTORY))
                            .getEntity().isOWLDatatype()).isTrue();
    }

    private boolean inSignature(OWLEntity entity) {
        return registry.get(projectId).indexes().get(EntitiesInProjectSignatureIndex.class)
                       .containsEntityInSignature(entity);
    }

    private static void assertInvalid(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOf(WpException.class).extracting("code").isEqualTo("INVALID_REQUEST");
    }
}
