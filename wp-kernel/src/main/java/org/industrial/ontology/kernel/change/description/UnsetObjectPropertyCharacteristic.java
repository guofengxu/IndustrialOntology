package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.UnsetObjectPropertyCharacteristic}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record UnsetObjectPropertyCharacteristic(@Nonnull OWLObjectProperty property, @Nonnull ObjectPropertyCharacteristic characteristic) implements StructuredChangeDescription {

    public UnsetObjectPropertyCharacteristic {
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(characteristic, "Null characteristic");
    }

    public UnsetObjectPropertyCharacteristic get(@Nonnull OWLObjectProperty property, @Nonnull ObjectPropertyCharacteristic characteristic) {
        return new UnsetObjectPropertyCharacteristic(property, characteristic);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "UnsetObjectPropertyCharacteristic";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Removed %s property characteristic from %s", getCharacteristic().getDisplayName(), getProperty());
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
