package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLDataProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.SetDataPropertyFunctional}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record SetDataPropertyFunctional(@Nonnull OWLDataProperty property) implements StructuredChangeDescription {

    public SetDataPropertyFunctional {
        Objects.requireNonNull(property, "Null property");
    }

    public static SetDataPropertyFunctional get(@Nonnull OWLDataProperty property) {
        return new SetDataPropertyFunctional(property);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "SetDataPropertyFunctional";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Made property %s functional", getProperty());
    }

    @Nonnull
    public OWLDataProperty getProperty() {
        return property;
    }
}
