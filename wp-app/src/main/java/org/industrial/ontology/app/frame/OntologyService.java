package org.industrial.ontology.app.frame;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PropertyAnnotationValue;
import org.industrial.ontology.domain.frame.State;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.change.FixedChangeListGenerator;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY_ANNOTATIONS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * A project's ontologies and their annotations (docs/01 §5.1, stage S7): the legacy {@code GetRootOntologyId},
 * {@code GetOntologyFrames}, {@code GetOntologyAnnotations} and {@code SetOntologyAnnotations} handlers. The legacy
 * {@code GetAllOntologies} listed the ontologies of a project and of an upload for merging, which is not ported
 * before P4 (docs/01 §3.3).
 * <p>
 * Reading needs {@code ViewProject}; editing annotations needs {@code EditOntology} and
 * {@code EditOntologyAnnotations}, as before. An ontology is named by its IRI; without one the root ontology, where
 * new axioms go, is meant.
 */
public class OntologyService {

    public static final String ONTOLOGY_NOT_FOUND = "ONTOLOGY_NOT_FOUND";

    private final ProjectContexts projects;

    public OntologyService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The project's ontologies, the root ontology first and the others by short name.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public List<OntologySummary> getOntologies(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var root = context.defaultOntologyIdManager().getDefaultOntologyId();
            var shortForms = context.components().ontologyIriShortFormProvider();
            return context.indexes()
                          .get(ProjectOntologiesIndex.class)
                          .getOntologyIds()
                          .map(id -> new OntologySummary(id, shortForms.getShortForm(id), id.equals(root)))
                          .sorted(Comparator.comparing((OntologySummary summary) -> !summary.root())
                                            .thenComparing(OntologySummary::shortForm,
                                                           String.CASE_INSENSITIVE_ORDER))
                          .toList();
        });
    }

    /**
     * The annotations of the ontology, ordered as the legacy frame showed them.
     *
     * @param ontologyIri the ontology's IRI, or {@code null} for the root ontology
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@value #ONTOLOGY_NOT_FOUND} (404) if the project has no such ontology
     */
    @Nonnull
    public OntologyAnnotations getAnnotations(@Nonnull UserId caller,
                                              @Nonnull ProjectId projectId,
                                              @Nullable IRI ontologyIri) {
        return projects.read(caller, projectId, VIEW_PROJECT,
                             context -> annotations(context, ontologyId(context, ontologyIri)));
    }

    /**
     * Replaces the annotations {@code from} with {@code to} on the ontology in one revision by the caller: those of
     * {@code from} that are not in {@code to} are removed, those of {@code to} that are not in {@code from} added.
     *
     * @param ontologyIri the ontology's IRI, or {@code null} for the root ontology
     * @param commitMessage describes the revision; without one it is "Edited ontology annotations", as before
     * @return the ontology's annotations after the change
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology or its annotations
     * @throws WpException               {@value #ONTOLOGY_NOT_FOUND} (404) if the project has no such ontology
     */
    @Nonnull
    public ChangeOutcome<OntologyAnnotations> setAnnotations(@Nonnull UserId caller,
                                                             @Nonnull ProjectId projectId,
                                                             @Nullable IRI ontologyIri,
                                                             @Nonnull List<PlainPropertyAnnotationValue> from,
                                                             @Nonnull List<PlainPropertyAnnotationValue> to,
                                                             @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY, EDIT_ONTOLOGY_ANNOTATIONS);
        var ontologyId = ProjectContexts.read(context, ctx -> ontologyId(ctx, ontologyIri));
        var fromAnnotations = new LinkedHashSet<>(from);
        var toAnnotations = new LinkedHashSet<>(to);
        var dataFactory = context.dataFactory();
        var changes = new ArrayList<OntologyChange>();
        fromAnnotations.stream()
                       .filter(annotation -> !toAnnotations.contains(annotation))
                       .map(annotation -> dataFactory.getOWLAnnotation(annotation.getProperty(),
                                                                       annotation.getValue()))
                       .forEach(annotation -> changes.add(RemoveOntologyAnnotationChange.of(ontologyId,
                                                                                            annotation)));
        toAnnotations.stream()
                     .filter(annotation -> !fromAnnotations.contains(annotation))
                     .map(annotation -> dataFactory.getOWLAnnotation(annotation.getProperty(), annotation.getValue()))
                     .forEach(annotation -> changes.add(AddOntologyAnnotationChange.of(ontologyId, annotation)));
        var generator = GuardedChange.of(new FixedChangeListGenerator<>(changes, ontologyId,
                                                                        "Edited ontology annotations"))
                                     .withCommitMessage(commitMessage);
        return ProjectContexts.apply(context, caller, generator)
                              .map(id -> ProjectContexts.read(context, ctx -> annotations(ctx, id)));
    }

    private static OntologyAnnotations annotations(ProjectContext context, OWLOntologyID ontologyId) {
        var rendering = context.rendering();
        var rendered = context.indexes()
                              .get(OntologyAnnotationsIndex.class)
                              .getOntologyAnnotations(ontologyId)
                              .map(annotation -> PropertyAnnotationValue.get(
                                      rendering.getAnnotationPropertyData(annotation.getProperty()),
                                      rendering.getRendering(annotation.getValue()),
                                      State.ASSERTED))
                              .sorted(context.components().propertyValueComparator())
                              .toList();
        var plain = rendered.stream()
                            .map(value -> PlainPropertyAnnotationValue.get(value.getProperty().getEntity(),
                                                                           value.getValue()
                                                                                .asAnnotationValue()
                                                                                .orElseThrow()))
                            .toList();
        return new OntologyAnnotations(ontologyId, plain, rendered);
    }

    private static OWLOntologyID ontologyId(ProjectContext context, @Nullable IRI ontologyIri) {
        if (ontologyIri == null) {
            return context.defaultOntologyIdManager().getDefaultOntologyId();
        }
        return context.indexes()
                      .get(ProjectOntologiesIndex.class)
                      .getOntologyIds()
                      .filter(id -> id.getOntologyIRI().isPresent() && id.getOntologyIRI().get().equals(ontologyIri))
                      .findFirst()
                      .orElseThrow(() -> WpException.notFound(ONTOLOGY_NOT_FOUND,
                                                              "The project has no ontology " + ontologyIri));
    }

    /**
     * One of the project's ontologies.
     *
     * @param shortForm its short name, as Manchester syntax uses it
     * @param root      whether it is the root ontology, where new axioms go
     */
    public record OntologySummary(@Nonnull OWLOntologyID ontologyId, @Nonnull String shortForm, boolean root) {
    }

    /**
     * An ontology's annotations, plain for editing and rendered with display names, in the same order.
     */
    public record OntologyAnnotations(@Nonnull OWLOntologyID ontologyId,
                                      @Nonnull List<PlainPropertyAnnotationValue> annotations,
                                      @Nonnull List<PropertyAnnotationValue> rendered) {
    }
}
