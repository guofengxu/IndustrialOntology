package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.AnnotationPropertyFrame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/11/2012
 */
public record AnnotationPropertyFrame(@Nonnull OWLAnnotationPropertyData subject, @Nonnull ImmutableSet<PropertyAnnotationValue> propertyValues, @Nonnull ImmutableSet<OWLEntityData> domains, @Nonnull ImmutableSet<OWLEntityData> ranges) implements EntityFrame<OWLAnnotationPropertyData>, HasPropertyValueList {

    public AnnotationPropertyFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(propertyValues, "Null propertyValues");
        Objects.requireNonNull(domains, "Null domains");
        Objects.requireNonNull(ranges, "Null ranges");
    }

    @Nonnull
    public static AnnotationPropertyFrame empty(@Nonnull OWLAnnotationPropertyData subject) {
        return get(subject, ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
    }

    @Nonnull
    public static AnnotationPropertyFrame get(@Nonnull OWLAnnotationPropertyData subject, @Nonnull ImmutableSet<PropertyAnnotationValue> propertyValues, @Nonnull ImmutableSet<OWLEntityData> domains, @Nonnull ImmutableSet<OWLEntityData> ranges) {
        return new AnnotationPropertyFrame(subject, propertyValues, domains, ranges);
    }

    @Override
    public PropertyValueList getPropertyValueList() {
        return new PropertyValueList(getPropertyValues());
    }

    @Nonnull
    @Override
    public PlainAnnotationPropertyFrame toPlainFrame() {
        return PlainAnnotationPropertyFrame.get(getSubject().getEntity(), getPropertyValues().stream().map(PropertyAnnotationValue::toPlainPropertyValue).collect(toImmutableSet()), getDomains().stream().map(OWLEntityData::getEntity).map(OWLEntity::getIRI).collect(toImmutableSet()), getRanges().stream().map(OWLEntityData::getEntity).map(OWLEntity::getIRI).collect(toImmutableSet()));
    }

    @Nonnull
    public OWLAnnotationPropertyData getSubject() {
        return subject;
    }

    @Nonnull
    public ImmutableSet<PropertyAnnotationValue> getPropertyValues() {
        return propertyValues;
    }

    @Nonnull
    public ImmutableSet<OWLEntityData> getDomains() {
        return domains;
    }

    @Nonnull
    public ImmutableSet<OWLEntityData> getRanges() {
        return ranges;
    }
}
