package org.industrial.ontology.app.frame;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.industrial.ontology.kernel.frame.FrameUpdate;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * The frames of a project's entities (docs/01 §5.1, stage S7): the legacy {@code Get*Frame} handlers of classes,
 * object, data and annotation properties and named individuals, and their {@code Update*Frame} handlers.
 * <p>
 * Reading needs {@code ViewProject}; the legacy data property handler asked for {@code ViewChanges}, unlike the
 * other four. Updating needs {@code EditOntology}, as before, and is refused with 409 {@value #CONFLICT_FRAME_CHANGED}
 * when the frame that the client edited is no longer the project's: the legacy handlers applied the edit to whatever
 * the frame had become (docs/02 §4.3).
 */
public class FrameService {

    public static final String CONFLICT_FRAME_CHANGED = "CONFLICT_FRAME_CHANGED";

    private final ProjectContexts projects;

    public FrameService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The entity's frame, plain for editing and rendered with display names for showing.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for a datatype, which has no frame
     */
    @Nonnull
    public EntityFrameView getFrame(@Nonnull UserId caller, @Nonnull ProjectId projectId, @Nonnull OWLEntity subject) {
        requireFrameSubject(subject);
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var frame = plainFrame(context, subject);
            var components = context.components();
            var rendered = frame.toEntityFrame(components.newFrameRenderer(),
                                               components.propertyValueComparator());
            return new EntityFrameView(frame, rendered);
        });
    }

    /**
     * Replaces the frame {@code from} with {@code to} in one revision by the caller. Nothing changes when the two are
     * equal.
     *
     * @param commitMessage describes the revision; without one the edit is described from its changes
     * @return the frame's subject, with the revision if anything changed
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} if the two frames are not of the same entity;
     *                                   {@value #CONFLICT_FRAME_CHANGED} (409) if {@code from} is not the entity's
     *                                   frame any more
     */
    @Nonnull
    public ChangeOutcome<OWLEntity> updateFrame(@Nonnull UserId caller,
                                                @Nonnull ProjectId projectId,
                                                @Nonnull PlainEntityFrame from,
                                                @Nonnull PlainEntityFrame to,
                                                @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        var subject = from.getSubject();
        requireFrameSubject(subject);
        if (!subject.equals(to.getSubject()) || from.getClass() != to.getClass()) {
            throw WpException.invalidRequest("The frames from and to must be frames of the same entity");
        }
        if (subject.isBuiltIn()) {
            // An edit of the frame of owl:Thing could make it a subclass of other classes
            throw WpException.invalidRequest("The frames of built-in entities such as " + subject.getIRI()
                                                     + " cannot be edited");
        }
        if (from.equals(to)) {
            return new ChangeOutcome<>(subject, Optional.empty(), context.events().getCurrentTag());
        }
        var generator = GuardedChange.of(context.components().newFrameChangeGenerator(FrameUpdate.get(from, to)))
                                     .checking(() -> requireUnchanged(context, from))
                                     .withCommitMessage(commitMessage);
        return ProjectContexts.apply(context, caller, generator);
    }

    /**
     * The frame as {@link #getFrame} returns it: a class frame with the class's own outgoing relationships (the
     * legacy options), and an individual frame with minimised property values (the legacy handler's setting).
     */
    static PlainEntityFrame plainFrame(ProjectContext context, OWLEntity subject) {
        var components = context.components();
        if (subject instanceof OWLClass cls) {
            return components.classFrameProvider().getFrame(cls, ClassFrameTranslationOptions.defaultOptions());
        } else if (subject instanceof OWLObjectProperty property) {
            return components.newObjectPropertyFrameTranslator().getFrame(property);
        } else if (subject instanceof OWLDataProperty property) {
            return components.newDataPropertyFrameTranslator().getFrame(property);
        } else if (subject instanceof OWLAnnotationProperty property) {
            return components.newAnnotationPropertyFrameTranslator().getFrame(property);
        } else if (subject instanceof OWLNamedIndividual individual) {
            var translator = components.newNamedIndividualFrameTranslator();
            translator.setMinimizePropertyValues(true);
            return translator.getFrame(individual);
        }
        throw WpException.invalidRequest(subject.getEntityType().getName() + " entities have no frames");
    }

    /**
     * Runs while the change is generated, so that no other change can follow the comparison.
     */
    private static void requireUnchanged(ProjectContext context, PlainEntityFrame from) {
        var current = plainFrame(context, from.getSubject());
        if (!current.equals(from)) {
            throw new WpException(CONFLICT_FRAME_CHANGED, 409,
                                  "The frame of " + from.getSubject().getIRI()
                                          + " has changed since it was read; read it again and repeat the edit");
        }
    }

    private static void requireFrameSubject(OWLEntity subject) {
        if (subject.isOWLDatatype()) {
            throw WpException.invalidRequest("Datatypes have no frames");
        }
    }
}
