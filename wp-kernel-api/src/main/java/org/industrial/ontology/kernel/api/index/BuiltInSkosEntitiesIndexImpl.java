package org.industrial.ontology.kernel.api.index;



import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
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
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.BuiltInSkosEntitiesIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-27
 */
public class BuiltInSkosEntitiesIndexImpl implements BuiltInSkosEntitiesIndex {

    @Nonnull
    private final ImmutableSet<OWLAnnotationProperty> annotationProperties;

    @Nonnull
    private final ImmutableSet<OWLClass> classes;

    @Nonnull
    private final ImmutableSet<OWLObjectProperty> objectProperties;

    @Nonnull
    private final ImmutableSet<OWLDataProperty> dataProperties;
    public BuiltInSkosEntitiesIndexImpl(@Nonnull OWLDataFactory dataFactory) {
        annotationProperties = SKOSVocabulary.getAnnotationProperties(dataFactory)
                      .stream()
                      .collect(toImmutableSet());

        classes = SKOSVocabulary.getClasses(dataFactory)
                .stream()
                .collect(toImmutableSet());
        objectProperties = SKOSVocabulary.getObjectProperties(dataFactory)
                .stream()
                .collect(toImmutableSet());
        dataProperties = SKOSVocabulary.getDataProperties(dataFactory)
                .stream()
                .collect(toImmutableSet());
    }

    @Nonnull
    @Override
    public Stream<OWLEntity> getBuiltInEntities() {
        return Stream.of(annotationProperties, classes, objectProperties, dataProperties)
                .flatMap(Collection::stream);
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
