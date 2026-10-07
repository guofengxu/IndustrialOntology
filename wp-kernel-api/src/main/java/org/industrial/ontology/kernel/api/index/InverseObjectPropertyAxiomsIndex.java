package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.InverseObjectPropertyAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public interface InverseObjectPropertyAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(@Nonnull OWLObjectProperty property,
                                                                           @Nonnull OWLOntologyID ontologyId);
}
