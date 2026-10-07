package org.industrial.ontology.domain.watches;



import com.google.common.base.Objects;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.watches.Watch}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 Apr 2017
 */
public class Watch {

    public static final String USER_ID = "userId";

    public static final String ENTITY = "entity";

    public static final String TYPE = "type";

    private UserId userId;

    private OWLEntity entity;

    private WatchType type;

    private Watch() {
    }

    public Watch(@Nonnull UserId userId,
                 @Nonnull OWLEntity entity,
                 @Nonnull WatchType type) {
        this.userId = checkNotNull(userId);
        this.entity = checkNotNull(entity);
        this.type = checkNotNull(type);
    }

    @Nonnull
    public UserId getUserId() {
        return userId;
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    @Nonnull
    public WatchType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entity, type, userId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Watch)) {
            return false;
        }
        Watch other = (Watch) obj;
        return this.userId.equals(other.userId)
                && this.entity.equals(other.entity)
                && this.type.equals(other.type);
    }


    @Override
    public String toString() {
        return toStringHelper("Watch")
                .addValue(userId)
                .addValue(entity)
                .addValue(type)
                .toString();
    }
}
