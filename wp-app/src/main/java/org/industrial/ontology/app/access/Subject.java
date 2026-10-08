package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.Subject}.
 * <p>
 * The subject of a role assignment: a specific signed-in user, the guest user, or any signed-in user. In the
 * {@code RoleAssignments} collection a specific user is stored by user name and "any signed-in user" by leaving the
 * user name out.
 */
public sealed interface Subject permits Subject.AnySignedInUser, Subject.SpecificUser {

    @Nonnull
    static Subject forAnySignedInUser() {
        return AnySignedInUser.INSTANCE;
    }

    @Nonnull
    static Subject forUser(@Nonnull UserId userId) {
        return new SpecificUser(userId);
    }

    /**
     * The user with this name; {@code null} stands for the guest user, as in {@link UserId#getUserId(String)}.
     */
    @Nonnull
    static Subject forUser(String userName) {
        return forUser(UserId.getUserId(userName));
    }

    @Nonnull
    static Subject forGuestUser() {
        return forUser(UserId.getGuest());
    }

    /**
     * Whether this subject is the guest user, whose assignments apply to users who are not signed in.
     */
    boolean isGuest();

    /**
     * Whether this subject stands for every signed-in user, that is every user except the guest user.
     */
    boolean isAnySignedInUser();

    /**
     * The user name of a specific user (the guest user included); empty for any signed-in user.
     */
    @Nonnull
    Optional<String> getUserName();

    @Nonnull
    Optional<UserId> getUserId();

    /**
     * Every signed-in user.
     */
    record AnySignedInUser() implements Subject {

        private static final AnySignedInUser INSTANCE = new AnySignedInUser();

        @Override
        public boolean isGuest() {
            return false;
        }

        @Override
        public boolean isAnySignedInUser() {
            return true;
        }

        @Nonnull
        @Override
        public Optional<String> getUserName() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<UserId> getUserId() {
            return Optional.empty();
        }
    }

    /**
     * One user, possibly the guest user.
     */
    record SpecificUser(@Nonnull UserId userId) implements Subject {

        public SpecificUser {
            checkNotNull(userId);
        }

        @Override
        public boolean isGuest() {
            return userId.isGuest();
        }

        @Override
        public boolean isAnySignedInUser() {
            return false;
        }

        @Nonnull
        @Override
        public Optional<String> getUserName() {
            return Optional.of(userId.getUserName());
        }

        @Nonnull
        @Override
        public Optional<UserId> getUserId() {
            return Optional.of(userId);
        }
    }
}
