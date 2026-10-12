package org.industrial.ontology.app.entity;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link IndividualsService} (07 5.1-4) on the pizza project.
 */
class IndividualsServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private IndividualsService individuals;

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
        individuals = context.bean(IndividualsService.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void theIndividualsOfATypeShouldBeListedAPageAtATime() {
        var all = individuals.getIndividuals(BOB, projectId, null, InstanceRetrievalMode.ALL_INSTANCES, "",
                                             PageRequest.requestPageWithSize(1, 100));
        assertThat(all.type()).isEmpty();
        assertThat(all.individuals().getPageElements()).extracting(EntityNode::getEntity)
                                                       .contains(PizzaOntology.individual(DATA_FACTORY,
                                                                                          "ExampleMargherita"),
                                                                 PizzaOntology.individual(DATA_FACTORY, "Italy"));
        assertThat(all.count()).isEqualTo(all.individuals().getTotalElements());

        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var ofMargherita = individuals.getIndividuals(BOB, projectId, margherita,
                                                      InstanceRetrievalMode.DIRECT_INSTANCES, "",
                                                      PageRequest.requestPageWithSize(1, 100));
        assertThat(ofMargherita.type().orElseThrow().getBrowserText()).isEqualTo("Margherita");
        assertThat(ofMargherita.individuals().getPageElements())
                .extracting(EntityNode::getEntity)
                .containsExactly(PizzaOntology.individual(DATA_FACTORY, "ExampleMargherita"));

        var page = individuals.getIndividuals(BOB, projectId, null, InstanceRetrievalMode.ALL_INSTANCES, "",
                                              PageRequest.requestPageWithSize(2, 3));
        assertThat(page.individuals().getPageElements()).hasSize(3);
        assertThat(page.individuals().getPageNumber()).isEqualTo(2);
    }

    @Test
    void thePageThatContainsAnIndividualShouldComeWithItsTypes() {
        var example = PizzaOntology.individual(DATA_FACTORY, "ExampleMargherita");
        var page = individuals.getPageContaining(BOB, projectId, example, null,
                                                 InstanceRetrievalMode.DIRECT_INSTANCES);
        assertThat(page.individual().getEntity()).isEqualTo(example);
        assertThat(page.individuals().getPageElements()).extracting(EntityNode::getEntity).contains(example);
        assertThat(page.types()).extracting(EntityNode::getEntity)
                                .contains(PizzaOntology.cls(DATA_FACTORY, "Margherita"));
        assertThatThrownBy(() -> individuals.getPageContaining(UserId.getUserId("carol"), projectId, example, null,
                                                               InstanceRetrievalMode.DIRECT_INSTANCES))
                .isInstanceOf(PermissionDeniedException.class);
    }
}
