package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.tag.Tag;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.tag.TagsManager}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 * One instance serves one project.
 */
public interface TagsManager {

    /** Tags on the entity, including tags assigned by tag criteria. */
    Collection<Tag> getTags(@Nonnull OWLEntity entity);
}
