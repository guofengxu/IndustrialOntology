package org.industrial.ontology.domain.issues;



import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.Mention}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26 Sep 16
 *
 * Represents the mention of some object in String
 */
public abstract class Mention {

    /**
     * If this mention mentions a UserId then this method returns the UserId.
     * @return The UserId.
     */
    @Nonnull
    public Optional<UserId> getMentionedUserId() {
        return Optional.empty();
    }
}
