package org.industrial.ontology.kernel.change.description;



import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.CreatedAnnotationProperties}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class CreatedAnnotationProperties extends AbstractCreatedProperties {

    @Nonnull
    public static CreatedAnnotationProperties get(@Nonnull ImmutableSet<OWLAnnotationProperty> properties,
                                              @Nonnull ImmutableSet<OWLAnnotationProperty> parentProperties) {
        return new AutoValue_CreatedAnnotationProperties(properties, parentProperties);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "CreatedAnnotationProperties";
    }
}
