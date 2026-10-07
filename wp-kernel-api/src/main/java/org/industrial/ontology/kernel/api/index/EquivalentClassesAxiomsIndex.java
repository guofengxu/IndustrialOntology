package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.EquivalentClassesAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public interface EquivalentClassesAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(@Nonnull
                                                                 OWLClass cls,
                                                                 @Nonnull
                                                                 OWLOntologyID ontologyID);
}
