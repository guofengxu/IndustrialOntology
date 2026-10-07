package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.SetObjectPropertyCharacteristic}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record SetObjectPropertyCharacteristic(@Nonnull OWLObjectProperty property, @Nonnull ObjectPropertyCharacteristic characteristic) implements StructuredChangeDescription {

    public SetObjectPropertyCharacteristic {
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(characteristic, "Null characteristic");
    }

    public SetObjectPropertyCharacteristic get(@Nonnull OWLObjectProperty property, @Nonnull ObjectPropertyCharacteristic characteristic) {
        return new SetObjectPropertyCharacteristic(property, characteristic);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "SetObjectPropertyCharacteristic";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Made property %s %s", getProperty(), getCharacteristic().getDisplayName());
    }

    @Nonnull
    public OWLObjectProperty getProperty() {
        return property;
    }

    @Nonnull
    public ObjectPropertyCharacteristic getCharacteristic() {
        return characteristic;
    }
}
