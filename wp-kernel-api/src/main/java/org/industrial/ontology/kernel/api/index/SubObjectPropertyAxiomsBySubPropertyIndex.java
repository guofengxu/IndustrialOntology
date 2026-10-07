package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.SubObjectPropertyAxiomsBySubPropertyIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public interface SubObjectPropertyAxiomsBySubPropertyIndex extends Index {

    @Nonnull
    Stream<OWLSubObjectPropertyOfAxiom> getSubPropertyOfAxioms(@Nonnull
                                                               OWLObjectProperty property,
                                                               @Nonnull
                                                               OWLOntologyID ontologyId);
}
