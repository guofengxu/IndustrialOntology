package org.industrial.ontology.kernel.change;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.event.ClassFrameChangedEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Acceptance test of P0-08 (docs/05): a change applied through the {@link ChangeManager} of a real
 * {@link ProjectContext} reaches every part of the kernel. Creating a class makes it visible in the indexes and the
 * class hierarchy, appends a revision, puts {@code ClassFrameChanged} and {@code EntityHierarchyChanged} into the
 * event bucket and makes the class searchable in Lucene. The 100-writer lock test is
 * {@link ChangeManagerConcurrencyIT}.
 */
public class ChangeManagerIT {

    @TempDir
    Path dataDirectory;

    private ProjectKernelFixture kernel;

    private ProjectContext context;

    @BeforeEach
    public void setUp() {
        kernel = new ProjectKernelFixture(dataDirectory);
        context = kernel.open(ProjectKernelFixture.freshProjectId());
    }

    @AfterEach
    public void tearDown() throws Exception {
        context.close();
        kernel.close();
    }

    @Test
    public void shouldPropagateCreatedClassToIndexesRevisionsEventsAndSearch() {
        var pizza = createClass("Pizza", ImmutableSet.of());
        var tagBeforeChange = context.events().getCurrentTag();

        var margherita = createClass("Margherita", ImmutableSet.of(pizza));

        // Indexes and hierarchy
        var indexes = context.indexes();
        var ontologyId = context.defaultOntologyIdManager().getDefaultOntologyId();
        assertThat(indexes.get(EntitiesInProjectSignatureIndex.class).containsEntityInSignature(margherita), is(true));
        assertThat(indexes.get(SubClassOfAxiomsBySubClassIndex.class)
                          .getSubClassOfAxiomsForSubClass(margherita, ontologyId)
                          .map(OWLSubClassOfAxiom::getSuperClass)
                          .collect(toList()),
                   contains(pizza));
        assertThat(indexes.get(ClassFrameAxiomsIndex.class)
                          .getFrameAxioms(margherita, ClassFrameAxiomsIndex.AnnotationsTreatment.INCLUDE_ANNOTATIONS),
                   is(not(empty())));
        assertThat(context.hierarchies().classHierarchy().getChildren(pizza), contains(margherita));

        // Revisions
        var revisions = context.revisionManager().getRevisions();
        assertThat(revisions.size(), is(2));
        assertThat(context.revisionManager().getCurrentRevision().getValue(), is(2L));
        assertThat(declaredEntities(revisions.get(1).getChanges()), hasItem(margherita));

        // Events
        var events = context.events().getEventsFromTag(tagBeforeChange).events();
        assertThat(events.stream()
                         .filter(ClassFrameChangedEvent.class::isInstance)
                         .map(event -> ((ClassFrameChangedEvent) event).entity())
                         .collect(toSet()),
                   hasItem(margherita));
        assertThat(hasClassHierarchyEvent(events), is(true));

        // Search
        assertThat(context.dictionary().getShortForm(margherita), is("Margherita"));
        assertThat(search("marg"), contains(margherita));
        assertThat(search("piz"), contains(pizza));
    }

    @Test
    public void shouldMakeNewLabelSearchableImmediately() {
        var pizza = createClass("Pizza", ImmutableSet.of());
        assertThat(search("quattro"), is(empty()));

        var quattroFormaggi = createClass("Quattro Formaggi", ImmutableSet.of(pizza));

        assertThat(search("quattro"), contains(quattroFormaggi));
        assertThat(search("formag"), contains(quattroFormaggi));
    }

    private OWLClass createClass(String name, ImmutableSet<OWLClass> parents) {
        return ProjectKernelFixture.createClass(context, name, parents);
    }

    private List<OWLEntity> search(String text) {
        return ProjectKernelFixture.searchClasses(context, text);
    }

    private static Set<OWLEntity> declaredEntities(List<OntologyChange> changes) {
        return changes.stream()
                      .filter(OntologyChange::isAddAxiom)
                      .map(OntologyChange::getAxiomOrThrow)
                      .filter(OWLDeclarationAxiom.class::isInstance)
                      .map(axiom -> ((OWLDeclarationAxiom) axiom).getEntity())
                      .collect(toSet());
    }

    private static boolean hasClassHierarchyEvent(List<ProjectEvent> events) {
        return events.stream()
                     .filter(EntityHierarchyChangedEvent.class::isInstance)
                     .map(event -> ((EntityHierarchyChangedEvent) event).getHierarchyId())
                     .anyMatch(HierarchyId.CLASS_HIERARCHY::equals);
    }
}
