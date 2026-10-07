package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.watches.Watch;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.watches.WatchRemovedEvent}.
 */
@JsonTypeName("WatchRemoved")
public record WatchRemovedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("watch") Watch watch) implements ProjectEvent {

    public WatchRemovedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(watch, "watch");
    }

    public Watch getWatch() {
        return watch;
    }

    @JsonIgnore
    public UserId getUserId() {
        return watch.getUserId();
    }
}
