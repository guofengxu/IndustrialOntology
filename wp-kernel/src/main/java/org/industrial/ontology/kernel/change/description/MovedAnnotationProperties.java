package org.industrial.ontology.kernel.change.description;



import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.MovedAnnotationProperties}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class MovedAnnotationProperties extends AbstractMovedProperties {

    public static MovedAnnotationProperties get(@Nonnull ImmutableSet<OWLAnnotationProperty> properties,
                                                @Nonnull ImmutableSet<OWLAnnotationProperty> from,
                                                @Nonnull OWLAnnotationProperty to) {
        return new AutoValue_MovedAnnotationProperties(properties,
                                                       from,
                                                       to);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "MovedAnnotationProperties";
    }
}
