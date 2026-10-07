package org.industrial.ontology.kernel.change.description;



import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.StructuredChangeDescription}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public interface StructuredChangeDescription {

    @Nonnull
    String getTypeName();

    @Nonnull
    String formatDescription(@Nonnull OWLObjectStringFormatter formatter);
}
