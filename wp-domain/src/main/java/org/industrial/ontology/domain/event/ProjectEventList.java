package org.industrial.ontology.domain.event;

import org.industrial.ontology.domain.core.ProjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.ProjectEventList}.
 * <p>
 * An {@link EventList} whose events all belong to one project.
 */
public record ProjectEventList(ProjectId projectId, EventTag startTag, List<ProjectEvent> events, EventTag endTag) {

    public ProjectEventList {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(startTag, "startTag");
        Objects.requireNonNull(endTag, "endTag");
        events = List.copyOf(events);
    }

    public static Builder builder(EventTag startTag, ProjectId projectId, EventTag endTag) {
        return new Builder(startTag, projectId, endTag);
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public List<ProjectEvent> getEvents() {
        return events;
    }

    public static final class Builder {

        private final ProjectId projectId;

        private final EventTag startTag;

        private final EventTag endTag;

        private final List<ProjectEvent> events = new ArrayList<>();

        public Builder(EventTag startTag, ProjectId projectId, EventTag endTag) {
            this.startTag = startTag;
            this.projectId = projectId;
            this.endTag = endTag;
        }

        public Builder addEvent(ProjectEvent event) {
            if (!event.projectId().equals(projectId)) {
                throw new IllegalArgumentException("event source is not equal to this builder's projectId");
            }
            events.add(event);
            return this;
        }

        public Builder addEvents(List<? extends ProjectEvent> events) {
            events.forEach(this::addEvent);
            return this;
        }

        public ProjectEventList build() {
            return new ProjectEventList(projectId, startTag, events, endTag);
        }
    }
}
