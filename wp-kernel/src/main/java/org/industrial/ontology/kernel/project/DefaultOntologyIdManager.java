package org.industrial.ontology.kernel.project;



import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.DefaultOntologyIdManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public interface DefaultOntologyIdManager extends Index {

    @Nonnull
    OWLOntologyID getDefaultOntologyId();
}
