package org.industrial.ontology.kernel.api.index;



import javax.annotation.Nonnull;

import java.util.Collection;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DependentIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-11
 */
public interface DependentIndex extends Index {

    @Nonnull
    Collection<Index> getDependencies();
}
