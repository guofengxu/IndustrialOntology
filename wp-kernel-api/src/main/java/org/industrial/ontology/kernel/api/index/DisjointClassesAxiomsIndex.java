package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DisjointClassesAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public interface DisjointClassesAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLDisjointClassesAxiom> getDisjointClassesAxioms(@Nonnull OWLClass cls, OWLOntologyID ontologyId);
}
