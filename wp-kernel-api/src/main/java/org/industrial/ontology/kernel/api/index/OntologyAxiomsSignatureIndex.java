package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.OntologyAxiomsSignatureIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-05
 */
public interface OntologyAxiomsSignatureIndex extends Index {

    boolean containsEntityInOntologyAxiomsSignature(@Nonnull OWLEntity entity,
                                                    @Nonnull OWLOntologyID ontologyId);

    <E extends OWLEntity> Stream<E> getOntologyAxiomsSignature(@Nonnull EntityType<E> type,
                                                               @Nonnull OWLOntologyID ontologyId);
}
