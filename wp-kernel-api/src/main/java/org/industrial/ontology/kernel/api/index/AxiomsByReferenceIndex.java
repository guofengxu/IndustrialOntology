package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.AxiomsByReferenceIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
public interface AxiomsByReferenceIndex extends Index {

    /**
     * Gets the axioms that reference one or more of the specified entities.  This includes logical and non-logical
     * axioms that have any of the entities in their signature.  This also includes annotation axioms that
     * reference one or more IRIs equal to IRIs of the entities in the specified set.
     * @param entities The entities.
     * @param ontologyId The ontology Id
     */
    @Nonnull
    Stream<OWLAxiom> getReferencingAxioms(@Nonnull Collection<OWLEntity> entities,
                                          @Nonnull OWLOntologyID ontologyId);
}
