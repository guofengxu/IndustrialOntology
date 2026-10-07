package org.industrial.ontology.kernel.change.description;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AbstractCreatedProperties}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public abstract class AbstractCreatedProperties implements StructuredChangeDescription {


    public abstract ImmutableSet<? extends OWLProperty> getProperties();

    public abstract ImmutableSet<? extends OWLProperty> getParentProperties();

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        if(getParentProperties().isEmpty()) {
            if(getProperties().size() == 1) {
                return formatter.formatString("Created property %s", getProperties());
            }
            else {
                return formatter.formatString("Created properties %s", getProperties());
            }
        }
        else {
            if(getProperties().size() == 1) {
                return formatter.formatString("Created %s as a sub-property of %s", getProperties(), getParentProperties());
            }
            else {
                return formatter.formatString("Created %s as a sub-property of %s", getProperties(), getParentProperties());
            }
        }
    }
}
