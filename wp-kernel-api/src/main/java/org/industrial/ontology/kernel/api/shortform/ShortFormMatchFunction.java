package org.industrial.ontology.kernel.api.shortform;



import com.google.common.primitives.ImmutableIntArray;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ShortFormMatchFunction}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 6 Apr 2018
 */
@FunctionalInterface
public interface ShortFormMatchFunction {

    @Nonnull
    ShortFormMatch createMatch(@Nonnull OWLEntity entity,
                               @Nonnull String shortForm,
                               int matchCount,
                               @Nonnull ImmutableIntArray matchPositions);

}
