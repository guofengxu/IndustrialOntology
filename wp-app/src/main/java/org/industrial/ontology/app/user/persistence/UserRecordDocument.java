package org.industrial.ontology.app.user.persistence;

import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A document of the {@code Users} collection, in the layout of the legacy {@code UserRecordConverter}:
 * {@code {_id: userName, realName, emailAddress, avatar?, salt?, saltedPasswordDigest?}}.
 * <p>
 * The legacy converter left {@code avatar} out when it was empty and read a missing e-mail address or avatar as the
 * empty string; this record does the same. The salt and the salted MD5 digest (lower-case hex) belong to the legacy
 * login, which is not ported (docs/01 §6): they are kept as they are so that a document written back is unchanged,
 * and users created from a Keycloak login have neither.
 * <p>
 * {@code localPasswordHash} is new (docs/01 §6): the BCrypt hash for the local fallback login, set by
 * {@code wp-cli create-admin --password}. Like the other optional fields it is left out when there is none, so legacy
 * documents are written back unchanged.
 *
 * @param userId               the user name, stored as {@code _id}
 * @param avatarUrl            stored as {@code avatar}; {@code null} when there is none
 * @param salt                 the legacy password salt, if any
 * @param saltedPasswordDigest the legacy password digest, if any
 * @param localPasswordHash    the BCrypt hash of the local password, if any
 */
@Document(UserRecordDocument.COLLECTION)
public record UserRecordDocument(@Id @Nonnull String userId,
                                 @Nonnull String realName,
                                 @Nonnull String emailAddress,
                                 @Field("avatar") @Nullable String avatarUrl,
                                 @Nullable String salt,
                                 @Nullable String saltedPasswordDigest,
                                 @Nullable String localPasswordHash) {

    public static final String COLLECTION = "Users";

    public static final String REAL_NAME = "realName";

    public static final String EMAIL_ADDRESS = "emailAddress";

    public static final String LOCAL_PASSWORD_HASH = "localPasswordHash";

    public UserRecordDocument {
        Objects.requireNonNull(userId, "userId");
        realName = Objects.requireNonNullElse(realName, "");
        emailAddress = Objects.requireNonNullElse(emailAddress, "");
        avatarUrl = avatarUrl == null || avatarUrl.isEmpty() ? null : avatarUrl;
    }

    /**
     * A user without the legacy password fields, as a Keycloak login creates it.
     */
    @Nonnull
    public static UserRecordDocument of(@Nonnull UserId userId,
                                        @Nonnull String realName,
                                        @Nonnull String emailAddress,
                                        @Nonnull String avatarUrl) {
        return new UserRecordDocument(userId.getUserName(), realName, emailAddress, avatarUrl, null, null, null);
    }

    @Nonnull
    public UserRecordDocument withEmailAddress(@Nonnull String emailAddress) {
        return new UserRecordDocument(userId, realName, emailAddress, avatarUrl, salt, saltedPasswordDigest,
                                      localPasswordHash);
    }

    @Nonnull
    public UserRecordDocument withLocalPasswordHash(@Nullable String localPasswordHash) {
        return new UserRecordDocument(userId, realName, emailAddress, avatarUrl, salt, saltedPasswordDigest,
                                      localPasswordHash);
    }

    @Nonnull
    public UserId getUserId() {
        return UserId.getUserId(userId);
    }

    /**
     * Without the password fields, so that a logged record does not carry them.
     */
    @Override
    public String toString() {
        return "UserRecordDocument[userId=" + userId + ", realName=" + realName + ", emailAddress=" + emailAddress
                + ", avatarUrl=" + avatarUrl + "]";
    }

    /**
     * The avatar URL, or the empty string, as the legacy {@code UserRecord.getAvatarUrl()} returned it.
     */
    @Nonnull
    public String getAvatarUrl() {
        return avatarUrl == null ? "" : avatarUrl;
    }
}
