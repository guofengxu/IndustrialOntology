package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.EventTag}.
 * <p>
 * A monotonically increasing cursor into a project's event stream; clients resume with the last tag they saw
 * (the SSE {@code id}, docs/01 §5.2).
 */
public record EventTag(int ordinal) implements Comparable<EventTag> {

    private static final EventTag FIRST = new EventTag(0);

    public static EventTag getFirst() {
        return FIRST;
    }

    @JsonCreator
    public static EventTag get(int index) {
        return new EventTag(index);
    }

    public EventTag next() {
        return get(ordinal + 1);
    }

    @JsonValue
    public int getOrdinal() {
        return ordinal;
    }

    public boolean isGreaterOrEqualTo(EventTag tag) {
        return this.ordinal >= tag.ordinal;
    }

    @Override
    public int compareTo(EventTag o) {
        return Integer.compare(this.ordinal, o.ordinal);
    }
}
