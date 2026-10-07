package org.industrial.ontology.domain.issues.mention;



import org.industrial.ontology.domain.issues.Mention;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;

import java.util.Optional;
import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.mention.UserIdMention}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 16
 */
public class UserIdMention extends Mention {

    @Nonnull
    private UserId userId;


    public UserIdMention(@Nonnull UserId userId) {
        this.userId = userId;
    }

    private UserIdMention() {
    }

    @Nonnull
    public UserId getUserId() {
        return userId;
    }

    /**
     * If this mention mentions a UserId then this method returns the UserId.
     *
     * @return The UserId.
     */
    @Nonnull
    @Override
    public Optional<UserId> getMentionedUserId() {
        return Optional.of(userId);
    }

    @Override
    public String toString() {
        return toStringHelper("UserIdMention")
                .addValue(userId)
                .toString();
    }
}
