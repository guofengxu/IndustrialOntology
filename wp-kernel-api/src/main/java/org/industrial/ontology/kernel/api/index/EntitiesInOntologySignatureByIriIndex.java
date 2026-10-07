package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.EntitiesInOntologySignatureByIriIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-05
 */
public interface EntitiesInOntologySignatureByIriIndex extends Index {

    @Nonnull
    Stream<OWLEntity> getEntitiesInSignature(@Nonnull IRI iri,
                                             @Nonnull OWLOntologyID ontologyId);
}
