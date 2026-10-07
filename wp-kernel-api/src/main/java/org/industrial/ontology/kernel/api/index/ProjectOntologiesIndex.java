package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ProjectOntologiesIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
public interface ProjectOntologiesIndex extends Index {

    /**
     * Gets the ontology Ids of project ontologies
     * @return A stream of ids that represent the ids of project ontologies
     */
    @Nonnull
    Stream<OWLOntologyID> getOntologyIds();
}
