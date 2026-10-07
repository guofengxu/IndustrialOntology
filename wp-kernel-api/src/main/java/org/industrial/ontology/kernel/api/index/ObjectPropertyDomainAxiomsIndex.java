package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ObjectPropertyDomainAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public interface ObjectPropertyDomainAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(@Nonnull
                                                                       OWLObjectProperty property,
                                                                       @Nonnull
                                                                       OWLOntologyID ontologyId);
}
