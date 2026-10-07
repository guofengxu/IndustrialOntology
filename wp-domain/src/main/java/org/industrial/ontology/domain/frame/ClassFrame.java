package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import javax.annotation.Nonnull;
import java.io.Serializable;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ClassFrame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/11/2012
 * <p>
 * A class frame describes some class in terms of other classEntries and property values.
 * </p>
 */
public record ClassFrame(@Nonnull OWLClassData subject, @Nonnull ImmutableSet<OWLClassData> classEntries, @Nonnull ImmutableSet<PropertyValue> propertyValues) implements EntityFrame<OWLClassData>, Serializable, HasPropertyValueList, HasPropertyValues, HasAnnotationPropertyValues, HasLogicalPropertyValues {

    public ClassFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(classEntries, "Null classEntries");
        Objects.requireNonNull(propertyValues, "Null propertyValues");
    }

    @Nonnull
    public static ClassFrame get(@Nonnull OWLClassData subject, @Nonnull ImmutableSet<OWLClassData> classEntries, @Nonnull ImmutableSet<PropertyValue> propertyValues) {
        return new ClassFrame(subject, classEntries, propertyValues);
    }

    public static ClassFrame empty(@Nonnull OWLClassData subject) {
        return get(subject, ImmutableSet.of(), ImmutableSet.of());
    }

    @Nonnull
    @Override
    public PropertyValueList getPropertyValueList() {
        return new PropertyValueList(getPropertyValues());
    }

    @Nonnull
    public ImmutableSet<PropertyAnnotationValue> getAnnotationPropertyValues() {
        return getPropertyValueList().getAnnotationPropertyValues();
    }

    @Nonnull
    public ImmutableList<PropertyValue> getLogicalPropertyValues() {
        return getPropertyValueList().getLogicalPropertyValues();
    }

    @Override
    @Nonnull
    public PlainClassFrame toPlainFrame() {
        return PlainClassFrame.get(getSubject().getEntity(), getClassEntries().stream().map(OWLClassData::getEntity).collect(toImmutableSet()), getPropertyValues().stream().map(PropertyValue::toPlainPropertyValue).collect(toImmutableSet()));
    }

    /**
     * Gets the subject of this class frame.
     *
     * @return The subject.  Not {@code null}.
     */
    @Nonnull
    public OWLClassData getSubject() {
        return subject;
    }

    @Nonnull
    public ImmutableSet<OWLClassData> getClassEntries() {
        return classEntries;
    }

    /**
     * Gets the {@link PropertyValue}s in this frame.
     *
     * @return The (possibly empty) set of property values in this frame. Not {@code null}.  The returned set is unmodifiable.
     */
    @Nonnull
    public ImmutableSet<PropertyValue> getPropertyValues() {
        return propertyValues;
    }
}
