package org.industrial.ontology.kernel.api.index;



import javax.annotation.Nonnull;

import java.util.stream.Stream;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.BuiltInEntitiesIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-27
 */
public interface BuiltInEntitiesIndex {

    /**
     * Gets all of the builtin entities
     */
    @Nonnull
    Stream<OWLEntity> getBuiltInEntities();

    /**
     * Gets the annotation properties in this builtin vocabulary
     */
    @Nonnull
    Stream<OWLAnnotationProperty> getAnnotationProperties();

    /**
     * Gets the classes in this builtin vocabulary
     */
    @Nonnull
    Stream<OWLClass> getClasses();

    /**
     * Gets the object properties in this builtin vocabulary
     */
    @Nonnull
    Stream<OWLObjectProperty> getObjectProperties();

    /**
     * Gets the data properties in this builtin vocabulary
     */
    @Nonnull
    Stream<OWLDataProperty> getDataProperties();
}
