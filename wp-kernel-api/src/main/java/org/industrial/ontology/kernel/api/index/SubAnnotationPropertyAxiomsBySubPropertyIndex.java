package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.SubAnnotationPropertyAxiomsBySubPropertyIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public interface SubAnnotationPropertyAxiomsBySubPropertyIndex extends Index {

    @Nonnull
    Stream<OWLSubAnnotationPropertyOfAxiom> getSubPropertyOfAxioms(@Nonnull OWLAnnotationProperty property,
                                                                   @Nonnull
                                                             OWLOntologyID ontologyId);
}
