package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import javax.annotation.Nonnull;
import java.io.Serializable;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.NamedIndividualFrame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 09/12/2012
 */
public record NamedIndividualFrame(@Nonnull OWLNamedIndividualData subject, @Nonnull ImmutableSet<OWLClassData> classes, @Nonnull ImmutableSet<PropertyValue> propertyValues, @Nonnull ImmutableSet<OWLNamedIndividualData> sameIndividuals) implements EntityFrame<OWLNamedIndividualData>, HasPropertyValues, HasAnnotationPropertyValues, HasLogicalPropertyValues, HasPropertyValueList, Serializable {

    public NamedIndividualFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(classes, "Null classes");
        Objects.requireNonNull(propertyValues, "Null propertyValues");
        Objects.requireNonNull(sameIndividuals, "Null sameIndividuals");
    }

    @Nonnull
    public static NamedIndividualFrame get(@Nonnull OWLNamedIndividualData subject, @Nonnull ImmutableSet<OWLClassData> namedTypes, @Nonnull ImmutableSet<PropertyValue> propertyValueList, @Nonnull ImmutableSet<OWLNamedIndividualData> sameIndividuals) {
        return new NamedIndividualFrame(subject, namedTypes, propertyValueList, sameIndividuals);
    }

    @Nonnull
    public static NamedIndividualFrame empty(@Nonnull OWLNamedIndividualData subject) {
        return get(subject, ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
    }

    @Override
    public ImmutableSet<PropertyAnnotationValue> getAnnotationPropertyValues() {
        return getPropertyValueList().getAnnotationPropertyValues();
    }

    @Nonnull
    @Override
    public ImmutableList<PropertyValue> getLogicalPropertyValues() {
        return getPropertyValueList().getLogicalPropertyValues();
    }

    @Nonnull
    @Override
    public PropertyValueList getPropertyValueList() {
        return new PropertyValueList(getPropertyValues());
    }

    @Nonnull
    @Override
    public PlainNamedIndividualFrame toPlainFrame() {
        return PlainNamedIndividualFrame.get(getSubject().getEntity(), getClasses().stream().map(OWLClassData::getEntity).collect(toImmutableSet()), getSameIndividuals().stream().map(OWLNamedIndividualData::getEntity).collect(toImmutableSet()), getPropertyValues().stream().map(PropertyValue::toPlainPropertyValue).collect(toImmutableSet()));
    }

    @Nonnull
    public OWLNamedIndividualData getSubject() {
        return subject;
    }

    @Nonnull
    public ImmutableSet<OWLClassData> getClasses() {
        return classes;
    }

    @Override
    @Nonnull
    public ImmutableSet<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    @Nonnull
    public ImmutableSet<OWLNamedIndividualData> getSameIndividuals() {
        return sameIndividuals;
    }
}
