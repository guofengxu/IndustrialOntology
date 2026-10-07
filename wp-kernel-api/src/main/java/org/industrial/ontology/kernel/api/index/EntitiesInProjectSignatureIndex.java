package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.EntitiesInProjectSignatureIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-17
 */
public interface EntitiesInProjectSignatureIndex extends Index {

    boolean containsEntityInSignature(@Nonnull OWLEntity entity);
}
