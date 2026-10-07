package org.industrial.ontology.kernel.change.description;



import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.CreatedObjectProperties}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class CreatedObjectProperties extends AbstractCreatedProperties {

    @Nonnull
    public static CreatedObjectProperties get(@Nonnull ImmutableSet<OWLObjectProperty> properties,
                                              @Nonnull ImmutableSet<OWLObjectProperty> parentProperties) {
        return new AutoValue_CreatedObjectProperties(properties, parentProperties);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "CreatedObjectProperties";
    }
}
