package org.industrial.ontology.app.entity;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * How a project shows its entities (docs/01 §5.1, stage S7): the legacy {@code GetEntityRendering} and
 * {@code GetEntityHtmlRendering} handlers. Both need {@code ViewProject}, as before.
 */
public class EntityRenderingService {

    private final ProjectContexts projects;

    public EntityRenderingService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The entity with its display names in the project's languages and whether it is deprecated.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public OWLEntityData getRendering(@Nonnull UserId caller, @Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> context.rendering().getRendering(entity));
    }

    /**
     * The entity's frame as HTML Manchester syntax, for the read-only description browser.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public EntityHtml getHtmlRendering(@Nonnull UserId caller,
                                       @Nonnull ProjectId projectId,
                                       @Nonnull OWLEntity entity) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var html = new StringBuilder();
            context.components().newEntityFrameHtmlRenderer().render(entity, html);
            return new EntityHtml(context.rendering().getRendering(entity), html.toString());
        });
    }

    /**
     * An entity's frame as HTML.
     */
    public record EntityHtml(@Nonnull OWLEntityData entity, @Nonnull String html) {
    }
}
