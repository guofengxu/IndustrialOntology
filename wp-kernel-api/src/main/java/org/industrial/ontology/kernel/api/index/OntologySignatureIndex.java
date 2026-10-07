package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.OntologySignatureIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public interface OntologySignatureIndex extends Index {

    /**
     * Gets the entities in the signature of the specified ontology.
     * @param ontologyID The ontologyId.
     * @return A stream of entities that represent the signature of the specified ontology.
     * An empty stream if the ontology is not known.  The stream will not contain duplicate
     * entities.
     */
    @Nonnull
    Stream<OWLEntity> getEntitiesInSignature(@Nonnull OWLOntologyID ontologyID);
}
