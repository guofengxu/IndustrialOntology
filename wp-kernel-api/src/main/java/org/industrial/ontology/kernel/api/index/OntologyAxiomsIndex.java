package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.OntologyAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
public interface OntologyAxiomsIndex extends Index {

    boolean containsAxiom(@Nonnull OWLAxiom axiom,
                          @Nonnull OWLOntologyID ontologyId);

    boolean containsAxiomIgnoreAnnotations(@Nonnull OWLAxiom axiom,
                                           @Nonnull OWLOntologyID ontologyId);

    @Nonnull
    Stream<OWLAxiom> getAxioms(@Nonnull OWLOntologyID ontologyId);
}
