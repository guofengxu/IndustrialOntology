package org.industrial.ontology.app.entity;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.search.RegexBudget;
import org.industrial.ontology.domain.bulkop.NewAnnotationData;
import org.industrial.ontology.domain.bulkop.Operation;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;

/**
 * Edits of many entities at once (docs/01 §5.1, stage S7): the legacy bulk operations {@code MoveToParent},
 * {@code SetAnnotationValue} and {@code EditAnnotationValues}, of which deleting annotations is one operation. Each
 * needs {@code EditOntology}, which only the legacy {@code ChangeManager} checked, and is one revision by the caller.
 */
public class BulkEditService {

    private final ProjectContexts projects;

    public BulkEditService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * Makes the classes subclasses of the parent only, replacing their named superclasses.
     *
     * @param commitMessage describes the revision; without one it names the parent
     * @return whether anything changed
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} for no entities or entities that are not classes; the
     *                                   legacy handler failed for them
     */
    @Nonnull
    public ChangeOutcome<Boolean> moveToParent(@Nonnull UserId caller,
                                               @Nonnull ProjectId projectId,
                                               @Nonnull Set<OWLEntity> entities,
                                               @Nonnull OWLEntity parent,
                                               @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        requireEntities(entities);
        if (!parent.isOWLClass() || entities.stream().anyMatch(entity -> !entity.isOWLClass())) {
            throw WpException.invalidRequest("Only classes can be moved to a parent class");
        }
        if (entities.stream().anyMatch(OWLEntity::isBuiltIn)) {
            throw WpException.invalidRequest("Built-in classes such as owl:Thing cannot be moved");
        }
        var classes = entities.stream().map(OWLEntity::asOWLClass).collect(ImmutableSet.toImmutableSet());
        OWLClass parentClass = parent.asOWLClass();
        var generator = context.components()
                               .moveClassesChangeListGeneratorFactory()
                               .create(classes, parentClass,
                                       message(commitMessage, "Moved classes to " + parentClass.getIRI()));
        return ProjectContexts.apply(context, caller, generator);
    }

    /**
     * Replaces the values that the entities have for the property with the value, keeping the annotations of the
     * annotations. As before, an entity without a value for the property gets none.
     *
     * @param commitMessage describes the revision; without one it names the property
     * @return the entities, as the legacy handler returned them
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} for no entities
     */
    @Nonnull
    public ChangeOutcome<Set<OWLEntity>> setAnnotationValue(@Nonnull UserId caller,
                                                            @Nonnull ProjectId projectId,
                                                            @Nonnull Set<OWLEntity> entities,
                                                            @Nonnull OWLAnnotationProperty property,
                                                            @Nonnull OWLAnnotationValue value,
                                                            @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        requireEntities(entities);
        var generator = context.components()
                               .setAnnotationValueChangeListGeneratorFactory()
                               .create(ImmutableSet.copyOf(entities), property, value,
                                       message(commitMessage, "Set the values of " + property.getIRI()));
        return ProjectContexts.apply(context, caller, generator);
    }

    /**
     * Edits the entities' literal annotations that match: those of the property (any if empty) whose value matches
     * the value expression (exactly, or as a regular expression that may match part of the value) and whose language
     * is the language tag (any if empty). {@link Operation#REPLACE} replaces each with the new data,
     * {@link Operation#AUGMENT} adds the new data next to it, {@link Operation#DELETE} deletes it. A regular expression
     * can refer to its groups in the new value, as {@link java.util.regex.Matcher#replaceAll} does.
     *
     * @param commitMessage describes the revision; without one it names the operation
     * @return whether anything changed
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} for no entities, a regular expression that does not
     *                                   compile or a new value that refers to a group that it does not have (the
     *                                   legacy handler failed with a server error); {@value RegexBudget#REGEX_TIMEOUT}
     *                                   (400) for an expression that takes too long
     */
    @Nonnull
    public ChangeOutcome<Boolean> editAnnotations(@Nonnull UserId caller,
                                                  @Nonnull ProjectId projectId,
                                                  @Nonnull Set<OWLEntity> entities,
                                                  @Nonnull Operation operation,
                                                  @Nonnull Optional<OWLAnnotationProperty> property,
                                                  @Nonnull Optional<String> valueExpression,
                                                  boolean regularExpression,
                                                  @Nonnull Optional<String> langTag,
                                                  @Nonnull NewAnnotationData newAnnotationData,
                                                  @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        requireEntities(entities);
        if (regularExpression && valueExpression.isPresent()) {
            try {
                Pattern.compile(valueExpression.get(), Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                throw WpException.invalidRequest("The value expression is not a regular expression: "
                                                         + e.getDescription());
            }
        }
        var generator = context.components()
                               .editAnnotationsChangeListGeneratorFactory()
                               .create(ImmutableSet.copyOf(entities),
                                       operation,
                                       property,
                                       valueExpression,
                                       regularExpression,
                                       langTag,
                                       newAnnotationData,
                                       message(commitMessage, operation.getPrintName() + " annotations"));
        try {
            return ProjectContexts.apply(context, caller, new RegexLimitedChanges<>(generator));
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            if (!regularExpression) {
                throw e;
            }
            // Matcher.replaceAll, while the changes are generated and before any is applied
            throw WpException.invalidRequest("The new value refers to a group that the expression does not have: "
                                                     + e.getMessage());
        }
    }

    /**
     * Generates the changes within {@link RegexBudget#REQUEST_BUDGET}: the expression is matched against every
     * literal while {@code ChangeManager} holds the project's change lock. Only the generation is limited; applying
     * the changes is not, so a timeout never interrupts a write.
     */
    private record RegexLimitedChanges<R>(ChangeListGenerator<R> delegate) implements ChangeListGenerator<R> {

        @Override
        public OntologyChangeList<R> generateChanges(ChangeGenerationContext context) {
            return RegexBudget.limited(() -> delegate.generateChanges(context));
        }

        @Override
        public R getRenamedResult(R result, RenameMap renameMap) {
            return delegate.getRenamedResult(result, renameMap);
        }

        @Nonnull
        @Override
        public String getMessage(ChangeApplicationResult<R> result) {
            return delegate.getMessage(result);
        }
    }

    private static void requireEntities(Set<OWLEntity> entities) {
        if (entities.isEmpty()) {
            throw WpException.invalidRequest("Name the entities to edit");
        }
    }

    private static String message(@Nullable String commitMessage, String defaultMessage) {
        return commitMessage == null || commitMessage.isBlank() ? defaultMessage : commitMessage.strip();
    }
}
