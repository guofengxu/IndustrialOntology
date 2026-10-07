package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import javax.annotation.Nonnull;
import java.io.Serializable;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.DataPropertyFrame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/04/2013
 */
public record DataPropertyFrame(OWLDataPropertyData subject, ImmutableSet<PropertyValue> propertyValues, @Nonnull ImmutableSet<OWLClassData> domains, @Nonnull ImmutableSet<OWLDatatypeData> ranges, boolean functional) implements EntityFrame<OWLDataPropertyData>, Serializable, HasPropertyValueList, HasPropertyValues, HasAnnotationPropertyValues, HasLogicalPropertyValues {

    public DataPropertyFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(propertyValues, "Null propertyValues");
        Objects.requireNonNull(domains, "Null domains");
        Objects.requireNonNull(ranges, "Null ranges");
    }

    @Nonnull
    public static DataPropertyFrame get(@Nonnull OWLDataPropertyData subject, @Nonnull ImmutableSet<PropertyValue> propertyValues, @Nonnull ImmutableSet<OWLClassData> domains, @Nonnull ImmutableSet<OWLDatatypeData> ranges, boolean functional) {
        return new DataPropertyFrame(subject, propertyValues, domains, ranges, functional);
    }

    @Nonnull
    public static DataPropertyFrame empty(@Nonnull OWLDataPropertyData subject) {
        return get(subject, ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), false);
    }

    @Override
    public PropertyValueList getPropertyValueList() {
        return new PropertyValueList(getPropertyValues());
    }

    @Override
    public ImmutableSet<PropertyAnnotationValue> getAnnotationPropertyValues() {
        return getPropertyValueList().getAnnotationPropertyValues();
    }

    @Override
    public ImmutableList<PropertyValue> getLogicalPropertyValues() {
        return getPropertyValueList().getLogicalPropertyValues();
    }

    @Nonnull
    @Override
    public PlainDataPropertyFrame toPlainFrame() {
        return PlainDataPropertyFrame.get(getSubject().getEntity(), getAnnotationPropertyValues().stream().map(PropertyAnnotationValue::toPlainPropertyValue).collect(toImmutableSet()), getDomains().stream().map(OWLClassData::getEntity).collect(toImmutableSet()), getRanges().stream().map(OWLDatatypeData::getEntity).collect(toImmutableSet()), isFunctional());
    }

    @Override
    public OWLDataPropertyData getSubject() {
        return subject;
    }

    @Override
    public ImmutableSet<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    @Nonnull
    public ImmutableSet<OWLClassData> getDomains() {
        return domains;
    }

    @Nonnull
    public ImmutableSet<OWLDatatypeData> getRanges() {
        return ranges;
    }

    public boolean isFunctional() {
        return functional;
    }
}
