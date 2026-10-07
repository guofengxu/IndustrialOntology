package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.data.FormSubject;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormFrame}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-13
 *
 * Represents the translation of form data into frame data
 */
public record FormFrame(@Nonnull FormSubject subject, @Nonnull ImmutableSet<OWLClass> classes, @Nonnull ImmutableSet<OWLClass> subClasses, @Nonnull ImmutableSet<OWLNamedIndividual> instances, @Nonnull ImmutableSet<PlainPropertyValue> propertyValues, @Nonnull ImmutableSet<FormFrame> nestedFrames) {

    public FormFrame {
        Objects.requireNonNull(subject, "Null subject");
        Objects.requireNonNull(classes, "Null classes");
        Objects.requireNonNull(subClasses, "Null subClasses");
        Objects.requireNonNull(instances, "Null instances");
        Objects.requireNonNull(propertyValues, "Null propertyValues");
        Objects.requireNonNull(nestedFrames, "Null nestedFrames");
    }

    @Nonnull
    public static FormFrame get(@Nonnull FormSubject formSubject, @Nonnull ImmutableSet<OWLClass> parents, @Nonnull ImmutableSet<OWLClass> subClasses, @Nonnull ImmutableSet<OWLNamedIndividual> instances, @Nonnull ImmutableSet<PlainPropertyValue> propertyValues, @Nonnull ImmutableSet<FormFrame> nestedFrames) {
        return new FormFrame(formSubject, parents, subClasses, instances, propertyValues, nestedFrames);
    }

    @Nonnull
    public static FormFrame get(@Nonnull FormSubject formSubject) {
        return get(formSubject, ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of(), ImmutableSet.of());
    }

    @Nonnull
    public FormSubject getSubject() {
        return subject;
    }

    @Nonnull
    public ImmutableSet<OWLClass> getClasses() {
        return classes;
    }

    @Nonnull
    public ImmutableSet<OWLClass> getSubClasses() {
        return subClasses;
    }

    @Nonnull
    public ImmutableSet<OWLNamedIndividual> getInstances() {
        return instances;
    }

    @Nonnull
    public ImmutableSet<PlainPropertyValue> getPropertyValues() {
        return propertyValues;
    }

    @Nonnull
    public ImmutableSet<FormFrame> getNestedFrames() {
        return nestedFrames;
    }
}
