package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A path through a hierarchy, root first.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.Path} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record Path<T>(@JsonProperty("elements") List<T> elements) implements Iterable<T> {

    public Path {
        elements = List.copyOf(elements);
    }

    public static <T> Path<T> emptyPath() {
        return new Path<>(List.of());
    }

    public static <T> Path<T> of(List<T> elements) {
        return new Path<>(elements);
    }

    @SafeVarargs
    public static <T> Path<T> of(T... elements) {
        return new Path<>(List.of(elements));
    }

    @JsonIgnore
    public Optional<T> getFirst() {
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }

    @JsonIgnore
    public Optional<T> getLast() {
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(elements.size() - 1));
    }

    /** The element just before the last one (the parent of the path's target), if any. */
    @JsonIgnore
    public Optional<T> getLastPredecessor() {
        return elements.size() < 2 ? Optional.empty() : Optional.of(elements.get(elements.size() - 2));
    }

    public int size() {
        return elements.size();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public List<T> asList() {
        return elements;
    }

    public <R> Path<R> transform(Function<T, R> function) {
        return new Path<>(elements.stream().map(function).collect(Collectors.toList()));
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }
}
