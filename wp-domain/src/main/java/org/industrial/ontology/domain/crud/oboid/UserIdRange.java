package org.industrial.ontology.domain.crud.oboid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.oboid.UserIdRange}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 30/07/2013
 */
public record UserIdRange(@Nonnull UserId userId, long start, long end) implements Serializable {

    public UserIdRange {
        Objects.requireNonNull(userId, "Null userId");
    }

    public static long getDefaultEnd() {
        return Long.MAX_VALUE;
    }

    public static long getDefaultStart() {
        return 0;
    }

    @Nonnull
    @JsonCreator
    public static UserIdRange get(@JsonProperty("userId") @Nonnull UserId userId, @JsonProperty("start") long start, @JsonProperty("end") long end) {
        return new UserIdRange(userId, start, end);
    }

    @Nonnull
    public UserId getUserId() {
        return userId;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
