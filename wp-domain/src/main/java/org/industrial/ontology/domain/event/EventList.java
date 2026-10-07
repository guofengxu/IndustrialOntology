package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.EventList}.
 * <p>
 * The events posted between two tags; {@code endTag} is where the next poll resumes.
 */
public record EventList<E>(EventTag startTag, List<E> events, EventTag endTag) {

    public EventList {
        Objects.requireNonNull(startTag, "startTag");
        Objects.requireNonNull(endTag, "endTag");
        events = List.copyOf(events);
    }

    public EventList(EventTag startTag, EventTag endTag) {
        this(startTag, List.of(), endTag);
    }

    public EventList(EventTag startTag, Collection<E> events, EventTag endTag) {
        this(startTag, List.copyOf(events), endTag);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return events.isEmpty();
    }

    public EventTag getStartTag() {
        return startTag;
    }

    public EventTag getEndTag() {
        return endTag;
    }

    public List<E> getEvents() {
        return events;
    }
}
