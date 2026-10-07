package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.CreatedClasses}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record CreatedClasses(@Nonnull ImmutableSet<OWLClass> classes, @Nonnull ImmutableSet<OWLClass> parentClasses) implements StructuredChangeDescription {

    public CreatedClasses {
        Objects.requireNonNull(classes, "Null classes");
        Objects.requireNonNull(parentClasses, "Null parentClasses");
    }

    private static final String CREATED_CLASSES = "CreatedClasses";

    public static String getAssociatedTypeName() {
        return CREATED_CLASSES;
    }

    @Nonnull
    public static CreatedClasses get(@Nonnull ImmutableSet<OWLClass> classes, @Nonnull ImmutableSet<OWLClass> parentClasses) {
        return new CreatedClasses(classes, parentClasses);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return getAssociatedTypeName();
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        if (getParentClasses().isEmpty()) {
            if (getClasses().size() == 1) {
                return formatter.formatString("Created class %s", getClasses());
            } else {
                return formatter.formatString("Created classes %s", getClasses());
            }
        } else {
            if (getClasses().size() == 1) {
                return formatter.formatString("Created %s as a subclass of %s", getClasses(), getParentClasses());
            } else {
                return formatter.formatString("Created %s as subclasses of %s", getClasses(), getParentClasses());
            }
        }
    }

    @Nonnull
    public ImmutableSet<OWLClass> getClasses() {
        return classes;
    }

    @Nonnull
    public ImmutableSet<OWLClass> getParentClasses() {
        return parentClasses;
    }
}
