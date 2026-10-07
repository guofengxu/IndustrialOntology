package org.industrial.ontology.domain.frame;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PropertyClassValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
@AutoValue
public abstract class PropertyClassValue extends ObjectPropertyValue {

    @Nonnull
    public static PropertyClassValue get(@Nonnull OWLObjectPropertyData property,
                                         @Nonnull OWLClassData value,
                                         @Nonnull State state) {
        return new AutoValue_PropertyClassValue(property, value, state);
    }

    @Override
    public abstract OWLObjectPropertyData getProperty();

    @Override
    public abstract OWLClassData getValue();

    @Override
    public abstract State getState();

    @Override
    public boolean isValueMostSpecific() {
        return false;
    }

    @Override
    public <R, E extends Throwable> R accept(PropertyValueVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public boolean isLogical() {
        return true;
    }

    @Override
    protected PropertyValue duplicateWithState(State state) {
        return PropertyClassValue.get(getProperty(), getValue(), state);
    }

    @Nonnull
    @Override
    public PlainPropertyClassValue toPlainPropertyValue() {
        return PlainPropertyClassValue.get(
                getProperty().getEntity(),
                getValue().getEntity(),
                getState()
        );
    }
}
