package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.OntologyAnnotationsSignatureIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-05
 */
public interface OntologyAnnotationsSignatureIndex extends Index {

    @Nonnull
    Stream<OWLAnnotationProperty> getOntologyAnnotationsSignature(@Nonnull OWLOntologyID ontologyId);

    boolean containsEntityInOntologyAnnotationsSignature(@Nonnull OWLEntity entity,
                                                         @Nonnull OWLOntologyID ontologyId);
}
