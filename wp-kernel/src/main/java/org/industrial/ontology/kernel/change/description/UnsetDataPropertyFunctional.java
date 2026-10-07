package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLDataProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.UnsetDataPropertyFunctional}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record UnsetDataPropertyFunctional(@Nonnull OWLDataProperty property) implements StructuredChangeDescription {

    public UnsetDataPropertyFunctional {
        Objects.requireNonNull(property, "Null property");
    }

    public static UnsetDataPropertyFunctional get(@Nonnull OWLDataProperty property) {
        return new UnsetDataPropertyFunctional(property);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "UnsetDataPropertyFunctional";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Made property %s non-functional", getProperty());
    }

    @Nonnull
    public OWLDataProperty getProperty() {
        return property;
    }
}
