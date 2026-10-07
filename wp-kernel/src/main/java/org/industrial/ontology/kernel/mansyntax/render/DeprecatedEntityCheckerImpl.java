package org.industrial.ontology.kernel.mansyntax.render;



import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesByEntityIndex;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.DeprecatedEntityCheckerImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27/01/15
 */
public class DeprecatedEntityCheckerImpl implements DeprecatedEntityChecker {

    @Nonnull
    private final DeprecatedEntitiesByEntityIndex deprecatedEntitiesByEntityIndex;

    public DeprecatedEntityCheckerImpl(@Nonnull DeprecatedEntitiesByEntityIndex deprecatedEntitiesByEntityIndex) {
        this.deprecatedEntitiesByEntityIndex = checkNotNull(deprecatedEntitiesByEntityIndex);
    }

    @Override
    public boolean isDeprecated(OWLEntity entity) {
        return deprecatedEntitiesByEntityIndex.isDeprecated(entity);
    }
}
