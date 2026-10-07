package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import javax.annotation.Nonnull;
import java.io.Serializable;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ObjectPropertyFrame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/12/2012
 */
public record ObjectPropertyFrame(@Nonnull OWLObjectPropertyData subject, @Nonnull ImmutableSet<PropertyAnnotationValue> annotationPropertyValues, @Nonnull ImmutableSet<OWLClassData> domains, @Nonnull ImmutableSet<OWLClassData> ranges, @Nonnull ImmutableSet<ObjectPropertyCharacteristic> characteristics, @Nonnull ImmutableSet<OWLObjectPropertyData> inverseProperties) implements EntityFrame<OWLObjectPropertyData>, HasAnnotationPropertyValues, Serializable {

    public ObjectPropertyFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(annotationPropertyValues, "Null annotationPropertyValues");
        Objects.requireNonNull(domains, "Null domains");
        Objects.requireNonNull(ranges, "Null ranges");
        Objects.requireNonNull(characteristics, "Null characteristics");
        Objects.requireNonNull(inverseProperties, "Null inverseProperties");
    }

    @Nonnull
    public static ObjectPropertyFrame get(@Nonnull OWLObjectPropertyData subject, @Nonnull ImmutableSet<PropertyAnnotationValue> annotationValues, @Nonnull ImmutableSet<OWLClassData> domains, @Nonnull ImmutableSet<OWLClassData> ranges, @Nonnull ImmutableSet<OWLObjectPropertyData> inverseProperties, @Nonnull ImmutableSet<ObjectPropertyCharacteristic> characteristics) {
        return new ObjectPropertyFrame(subject, annotationValues, domains, ranges, characteristics, inverseProperties);
    }

    @Nonnull
    public static ObjectPropertyFrame empty(@Nonnull OWLObjectPropertyData subject) {
        return get(subject, ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
    }

    @Nonnull
    @Override
    public PlainObjectPropertyFrame toPlainFrame() {
        return PlainObjectPropertyFrame.get(getSubject().getEntity(), getAnnotationPropertyValues().stream().map(PropertyAnnotationValue::toPlainPropertyValue).collect(toImmutableSet()), getCharacteristics(), getDomains().stream().map(OWLClassData::getEntity).collect(toImmutableSet()), getRanges().stream().map(OWLClassData::getEntity).collect(toImmutableSet()), getInverseProperties().stream().map(OWLObjectPropertyData::getEntity).collect(toImmutableSet()));
    }

    @Nonnull
    public OWLObjectPropertyData getSubject() {
        return subject;
    }

    @Override
    @Nonnull
    public ImmutableSet<PropertyAnnotationValue> getAnnotationPropertyValues() {
        return annotationPropertyValues;
    }

    @Nonnull
    public ImmutableSet<OWLClassData> getDomains() {
        return domains;
    }

    @Nonnull
    public ImmutableSet<OWLClassData> getRanges() {
        return ranges;
    }

    @Nonnull
    public ImmutableSet<ObjectPropertyCharacteristic> getCharacteristics() {
        return characteristics;
    }

    @Nonnull
    public ImmutableSet<OWLObjectPropertyData> getInverseProperties() {
        return inverseProperties;
    }
}
