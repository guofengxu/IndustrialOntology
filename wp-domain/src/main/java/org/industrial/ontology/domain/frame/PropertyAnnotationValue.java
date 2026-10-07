package org.industrial.ontology.domain.frame;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PropertyAnnotationValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
@AutoValue
public abstract class PropertyAnnotationValue extends PropertyValue {


    @Nonnull
    public static PropertyAnnotationValue get(@Nonnull OWLAnnotationPropertyData property,
                                              @Nonnull OWLPrimitiveData value,
                                              @Nonnull State state) {
        return new AutoValue_PropertyAnnotationValue(property, value, state);
    }


    @Override
    public abstract OWLAnnotationPropertyData getProperty();

    @Override
    public abstract OWLPrimitiveData getValue();

    @Override
    public abstract State getState();

    @Nonnull
    public Optional<OWLEntityData> getValueAsEntity() {
        OWLPrimitiveData primitiveData = getValue();
        if(primitiveData instanceof OWLEntityData) {
            return Optional.of((OWLEntityData) primitiveData);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isValueMostSpecific() {
        return true;
    }


    @Override
    public boolean isAnnotation() {
        return true;
    }

    @Override
    public boolean isLogical() {
        return false;
    }

    @Override
    protected PropertyValue duplicateWithState(State state) {
        return PropertyAnnotationValue.get(getProperty(), getValue(), state);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Nonnull
    @Override
    public PlainPropertyAnnotationValue toPlainPropertyValue() {
        return PlainPropertyAnnotationValue.get(
                getProperty().getEntity(),
                getValue().asAnnotationValue().get(),
                getState()
        );
    }

    @Override
    public <R, E extends Throwable> R accept(PropertyValueVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }
}
