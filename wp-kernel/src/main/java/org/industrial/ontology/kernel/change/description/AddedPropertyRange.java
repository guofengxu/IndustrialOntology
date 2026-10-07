package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedPropertyRange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedPropertyRange(@Nonnull OWLProperty property, @Nonnull OWLObject range) implements StructuredChangeDescription {

    public AddedPropertyRange {
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(range, "Null range");
    }

    public static AddedPropertyRange get(@Nonnull OWLProperty property, @Nonnull OWLObject range) {
        return new AddedPropertyRange(property, range);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedPropertyRange";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added %s to the range of %s", getRange(), getProperty());
    }

    @Nonnull
    public OWLProperty getProperty() {
        return property;
    }

    @Nonnull
    public OWLObject getRange() {
        return range;
    }
}
