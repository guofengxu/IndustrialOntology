package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.MovedClasses}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record MovedClasses(@Nonnull ImmutableSet<OWLClass> classes, @Nonnull ImmutableSet<OWLClass> from, @Nonnull OWLClass to) implements StructuredChangeDescription {

    public MovedClasses {
        Objects.requireNonNull(classes, "Null classes");
        Objects.requireNonNull(from, "Null from");
        Objects.requireNonNull(to, "Null to");
    }

    public static MovedClasses get(@Nonnull ImmutableSet<OWLClass> classes, @Nonnull ImmutableSet<OWLClass> from, @Nonnull OWLClass to) {
        return new MovedClasses(classes, from, to);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "MovedClasses";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        if (getClasses().size() == 1) {
            return formatter.formatString("Moved class %s from %s to %s", getClasses(), getFrom(), getTo());
        } else {
            return formatter.formatString("Moved classes %s from %s to %s", getClasses(), getFrom(), getTo());
        }
    }

    @Nonnull
    public ImmutableSet<OWLClass> getClasses() {
        return classes;
    }

    @Nonnull
    public ImmutableSet<OWLClass> getFrom() {
        return from;
    }

    @Nonnull
    public OWLClass getTo() {
        return to;
    }
}
