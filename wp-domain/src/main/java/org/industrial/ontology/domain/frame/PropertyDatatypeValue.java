package org.industrial.ontology.domain.frame;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PropertyDatatypeValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
@AutoValue
public abstract class PropertyDatatypeValue extends DataPropertyValue {


    @Nonnull
    public static PropertyDatatypeValue get(@Nonnull OWLDataPropertyData property,
                                            @Nonnull OWLDatatypeData datatype,
                                            @Nonnull State state) {
        return new AutoValue_PropertyDatatypeValue(property, datatype, state);
    }

    @Override
    public abstract OWLDataPropertyData getProperty();

    @Override
    public abstract OWLDatatypeData getValue();

    @Override
    public abstract State getState();

    @Override
    public boolean isValueMostSpecific() {
        return false;
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
        return PropertyDatatypeValue.get(getProperty(), getValue(), state);
    }

    @Nonnull
    @Override
    public PlainPropertyDatatypeValue toPlainPropertyValue() {
        return PlainPropertyDatatypeValue.get(getProperty().getEntity(),
                                              getValue().getEntity(),
                                              getState());
    }
}
