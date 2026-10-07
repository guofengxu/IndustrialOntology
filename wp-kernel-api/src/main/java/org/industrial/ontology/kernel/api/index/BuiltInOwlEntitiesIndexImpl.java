package org.industrial.ontology.kernel.api.index;



import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.stream.Stream;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.BuiltInOwlEntitiesIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-27
 */
public class BuiltInOwlEntitiesIndexImpl implements BuiltInOwlEntitiesIndex {


    @Nonnull
    private final ImmutableSet<OWLClass> classes;

    @Nonnull
    private final ImmutableSet<OWLObjectProperty> objectProperties;

    @Nonnull
    private final ImmutableSet<OWLDataProperty> dataProperties;

    @Nonnull
    private final ImmutableSet<OWLAnnotationProperty> annotationProperties;


    @Nonnull
    private final ImmutableSet<OWLEntity> builtInEntities;

    public BuiltInOwlEntitiesIndexImpl(@Nonnull OWLDataFactory dataFactory) {
        classes = ImmutableSet.of(
                dataFactory.getOWLThing(),
                dataFactory.getOWLNothing()
        );
        objectProperties = ImmutableSet.of(
                dataFactory.getOWLTopObjectProperty(),
                dataFactory.getOWLBottomObjectProperty()
        );
        dataProperties = ImmutableSet.of(
                dataFactory.getOWLTopDataProperty(),
                dataFactory.getOWLBottomDataProperty()
        );
        annotationProperties = ImmutableSet.of(
                dataFactory.getRDFSLabel(),
                dataFactory.getRDFSComment(),
                dataFactory.getRDFSIsDefinedBy(),
                dataFactory.getRDFSSeeAlso(),
                dataFactory.getOWLBackwardCompatibleWith(),
                dataFactory.getOWLIncompatibleWith()
        );
        builtInEntities = ImmutableSet.of(classes, objectProperties, dataProperties, annotationProperties)
                .stream()
                .flatMap(Collection::stream)
                .collect(toImmutableSet());
    }

    @Nonnull
    @Override
    public Stream<OWLEntity> getBuiltInEntities() {
        return builtInEntities.stream();
    }

    @Nonnull
    @Override
    public Stream<OWLAnnotationProperty> getAnnotationProperties() {
        return annotationProperties.stream();
    }

    @Nonnull
    @Override
    public Stream<OWLClass> getClasses() {
        return classes.stream();
    }

    @Nonnull
    @Override
    public Stream<OWLObjectProperty> getObjectProperties() {
        return objectProperties.stream();
    }

    @Nonnull
    @Override
    public Stream<OWLDataProperty> getDataProperties() {
        return dataProperties.stream();
    }
}
