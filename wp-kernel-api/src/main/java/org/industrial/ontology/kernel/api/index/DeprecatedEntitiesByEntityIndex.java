package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DeprecatedEntitiesByEntityIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public interface DeprecatedEntitiesByEntityIndex extends Index {

    boolean isDeprecated(@Nonnull OWLEntity entity);
}
