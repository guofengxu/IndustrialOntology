package org.industrial.ontology.domain.frame;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PropertyIndividualValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
@AutoValue
public abstract class PropertyIndividualValue extends ObjectPropertyValue {

    @Nonnull
    public static PropertyIndividualValue get(@Nonnull OWLObjectPropertyData property,
                                              @Nonnull OWLNamedIndividualData value,
                                              @Nonnull State state) {
        return new AutoValue_PropertyIndividualValue(property,
                                                     value,
                                                     state);
    }

    @Override
    public abstract OWLObjectPropertyData getProperty();

    @Override
    public abstract OWLNamedIndividualData getValue();

    @Override
    public abstract State getState();

    @Override
    public boolean isValueMostSpecific() {
        return true;
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
    public <R, E extends Throwable> R accept(PropertyValueVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }

    @Override
    protected PropertyValue duplicateWithState(State state) {
        return PropertyIndividualValue.get(getProperty(), getValue(), state);
    }

    @Nonnull
    @Override
    public PlainPropertyIndividualValue toPlainPropertyValue() {
        return PlainPropertyIndividualValue.get(
                getProperty().getEntity(),
                getValue().getEntity(),
                getState()
        );
    }
}
