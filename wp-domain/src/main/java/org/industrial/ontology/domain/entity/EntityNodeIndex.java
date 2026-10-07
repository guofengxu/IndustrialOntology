package org.industrial.ontology.domain.entity;



import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityNodeIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16 Aug 2018
 */
public interface EntityNodeIndex {

    Optional<EntityNode> getNode(@Nonnull OWLEntity entity);

    void updateNode(@Nonnull EntityNode entityNode);
}
