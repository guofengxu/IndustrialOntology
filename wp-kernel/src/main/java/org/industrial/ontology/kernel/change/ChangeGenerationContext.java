package org.industrial.ontology.kernel.change;



import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ChangeGenerationContext}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 */
public class ChangeGenerationContext {

    @Nonnull
    private final UserId userId;

    public ChangeGenerationContext(@Nonnull UserId userId) {
        this.userId = checkNotNull(userId);
    }

    @Nonnull
    public UserId getUserId() {
        return userId;
    }
}
