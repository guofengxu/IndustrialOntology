package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.EquivalentDataPropertiesAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public interface EquivalentDataPropertiesAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(@Nonnull OWLDataProperty property,
                                                                               @Nonnull OWLOntologyID ontologyId);
}
