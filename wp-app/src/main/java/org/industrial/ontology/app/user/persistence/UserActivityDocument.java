package org.industrial.ontology.app.user.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A document of the {@code UserActivity} collection, as Morphia wrote the legacy {@code UserActivityRecord}:
 * {@code {_id: userName, lastLogin: Date, lastLogout: Date, recentProjects: [{projectId, timestamp: Date}]}}.
 * The epoch stands for "unknown", as the legacy {@code UNKNOWN = 0}.
 *
 * @param userId stored as {@code _id}
 */
@Document(UserActivityDocument.COLLECTION)
public record UserActivityDocument(@Id @Nonnull String userId,
                                   @Nonnull Instant lastLogin,
                                   @Nonnull Instant lastLogout,
                                   @Nonnull List<RecentProject> recentProjects) {

    public static final String COLLECTION = "UserActivity";

    public static final String LAST_LOGIN = "lastLogin";

    public static final String LAST_LOGOUT = "lastLogout";

    public static final String RECENT_PROJECTS = "recentProjects";

    public UserActivityDocument {
        Objects.requireNonNull(userId, "userId");
        lastLogin = Objects.requireNonNullElse(lastLogin, Instant.EPOCH);
        lastLogout = Objects.requireNonNullElse(lastLogout, Instant.EPOCH);
        recentProjects = recentProjects == null ? List.of() : List.copyOf(recentProjects);
    }

    /**
     * A record with nothing known yet, as the legacy {@code UserActivityRecord.get(userId)}.
     */
    @Nonnull
    public static UserActivityDocument empty(@Nonnull UserId userId) {
        return new UserActivityDocument(userId.getUserName(), Instant.EPOCH, Instant.EPOCH, List.of());
    }

    @Nonnull
    public UserId getUserId() {
        return UserId.getUserId(userId);
    }

    /**
     * An entry of {@code recentProjects}, ordered as the legacy {@code RecentProjectRecord}: most recent first, then
     * by project id.
     */
    public record RecentProject(@Nonnull String projectId, @Nonnull Instant timestamp)
            implements Comparable<RecentProject> {

        private static final Comparator<RecentProject> order = Comparator.comparing(RecentProject::timestamp)
                                                                         .reversed()
                                                                         .thenComparing(RecentProject::projectId);

        public RecentProject {
            Objects.requireNonNull(projectId, "projectId");
            Objects.requireNonNull(timestamp, "timestamp");
        }

        @Nonnull
        public static RecentProject of(@Nonnull ProjectId projectId, long timestamp) {
            return new RecentProject(projectId.getId(), Instant.ofEpochMilli(timestamp));
        }

        @Nonnull
        public ProjectId getProjectId() {
            return ProjectId.get(projectId);
        }

        @Override
        public int compareTo(@Nonnull RecentProject other) {
            return order.compare(this, other);
        }
    }
}
