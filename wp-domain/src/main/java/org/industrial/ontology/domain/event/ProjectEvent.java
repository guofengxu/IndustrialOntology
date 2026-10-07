package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.industrial.ontology.domain.core.ProjectId;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.ProjectEvent}.
 * <p>
 * An event about one project: kept in the project's event buckets (cursor {@link EventTag}) and pushed to
 * clients over SSE. The legacy GWT {@code ProjectEvent<H>} / {@code WebProtegeEvent<H>} hierarchy is replaced
 * by this sealed interface and records (docs/01 §4). The JSON type name is the legacy class name without the
 * {@code Event} suffix (docs/01 §5.2).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClassFrameChangedEvent.class, name = "ClassFrameChanged"),
        @JsonSubTypes.Type(value = ObjectPropertyFrameChangedEvent.class, name = "ObjectPropertyFrameChanged"),
        @JsonSubTypes.Type(value = DataPropertyFrameChangedEvent.class, name = "DataPropertyFrameChanged"),
        @JsonSubTypes.Type(value = AnnotationPropertyFrameChangedEvent.class, name = "AnnotationPropertyFrameChanged"),
        @JsonSubTypes.Type(value = NamedIndividualFrameChangedEvent.class, name = "NamedIndividualFrameChanged"),
        @JsonSubTypes.Type(value = DatatypeFrameChangedEvent.class, name = "DatatypeFrameChanged"),
        @JsonSubTypes.Type(value = BrowserTextChangedEvent.class, name = "BrowserTextChanged"),
        @JsonSubTypes.Type(value = EntityDeprecatedChangedEvent.class, name = "EntityDeprecatedChanged"),
        @JsonSubTypes.Type(value = OntologyFrameChangedEvent.class, name = "OntologyFrameChanged"),
        @JsonSubTypes.Type(value = ProjectChangedEvent.class, name = "ProjectChanged"),
        @JsonSubTypes.Type(value = EntityHierarchyChangedEvent.class, name = "EntityHierarchyChanged"),
        @JsonSubTypes.Type(value = CommentPostedEvent.class, name = "CommentPosted"),
        @JsonSubTypes.Type(value = CommentUpdatedEvent.class, name = "CommentUpdated"),
        @JsonSubTypes.Type(value = DiscussionThreadCreatedEvent.class, name = "DiscussionThreadCreated"),
        @JsonSubTypes.Type(value = DiscussionThreadStatusChangedEvent.class, name = "DiscussionThreadStatusChanged"),
        @JsonSubTypes.Type(value = DisplayNameSettingsChangedEvent.class, name = "DisplayNameSettingsChanged"),
        @JsonSubTypes.Type(value = PermissionsChangedEvent.class, name = "PermissionsChanged"),
        @JsonSubTypes.Type(value = ProjectSettingsChangedEvent.class, name = "ProjectSettingsChanged"),
        @JsonSubTypes.Type(value = EntityTagsChangedEvent.class, name = "EntityTagsChanged"),
        @JsonSubTypes.Type(value = ProjectTagsChangedEvent.class, name = "ProjectTagsChanged"),
        @JsonSubTypes.Type(value = WatchAddedEvent.class, name = "WatchAdded"),
        @JsonSubTypes.Type(value = WatchRemovedEvent.class, name = "WatchRemoved"),
        @JsonSubTypes.Type(value = UserStartingViewingProjectEvent.class, name = "UserStartedViewingProject"),
        @JsonSubTypes.Type(value = UserStoppedViewingProjectEvent.class, name = "UserStoppedViewingProject"),
        @JsonSubTypes.Type(value = ProjectMovedToTrashEvent.class, name = "ProjectMovedToTrash"),
        @JsonSubTypes.Type(value = ProjectMovedFromTrashEvent.class, name = "ProjectMovedFromTrash")})
public sealed interface ProjectEvent permits EntityFrameChangedEvent, BrowserTextChangedEvent, EntityDeprecatedChangedEvent, OntologyFrameChangedEvent, ProjectChangedEvent, EntityHierarchyChangedEvent, CommentPostedEvent, CommentUpdatedEvent, DiscussionThreadCreatedEvent, DiscussionThreadStatusChangedEvent, DisplayNameSettingsChangedEvent, PermissionsChangedEvent, ProjectSettingsChangedEvent, EntityTagsChangedEvent, ProjectTagsChangedEvent, WatchAddedEvent, WatchRemovedEvent, UserStartingViewingProjectEvent, UserStoppedViewingProjectEvent, ProjectMovedToTrashEvent, ProjectMovedFromTrashEvent {

    ProjectId projectId();

    default ProjectId getProjectId() {
        return projectId();
    }

    /** Legacy accessor: GWT events exposed their project as the event source. */
    @JsonIgnore
    default ProjectId getSource() {
        return projectId();
    }
}
