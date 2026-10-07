package org.industrial.ontology.domain.frame;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLLiteralData;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PropertyLiteralValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
@AutoValue
public abstract class PropertyLiteralValue extends DataPropertyValue {

    @Nonnull
    public static PropertyLiteralValue get(@Nonnull OWLDataPropertyData property,
                                @Nonnull OWLLiteralData value,
                                @Nonnull State state) {
        return new AutoValue_PropertyLiteralValue(property, value, state);
    }

    @Override
    public abstract OWLDataPropertyData getProperty();

    @Override
    public abstract OWLLiteralData getValue();

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
        return PropertyLiteralValue.get(getProperty(), getValue(), state);
    }

    @Nonnull
    @Override
    public PlainPropertyLiteralValue toPlainPropertyValue() {
        return PlainPropertyLiteralValue.get(
                getProperty().getEntity(),
                getValue().getLiteral(),
                getState()
        );
    }
}
