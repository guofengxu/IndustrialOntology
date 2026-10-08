package org.industrial.ontology.app.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The users of the application (docs/01 §5.1): replaces the legacy {@code UserDetailsManager}, the handlers
 * {@code GetEmailAddress}, {@code SetEmailAddress} and {@code GetUserIdCompletions}, and the command-line
 * {@code SetupTools}. Login, passwords and sessions belong to Keycloak now; what remains here is the {@code Users}
 * record of each user, created when a user is first seen (docs/01 §6), and the local fallback password.
 * <p>
 * Methods that take a {@code caller} act for that user and check its permissions first (docs/07 5.1-16). The others
 * serve authentication, the command line and other services, and are never called with request data unchecked.
 */
public class UserService {

    /** As the legacy {@code GetUserIdCompletionsActionHandler}. */
    public static final int COMPLETIONS_LIMIT = 10;

    /** The most users {@link #findUsers} returns. */
    public static final int MAX_USERS = 100;

    /** BCrypt uses at most 72 bytes of a password; a longer one would be cut without notice. */
    static final int MAX_PASSWORD_BYTES = 72;

    /** The local password belongs to an administrator, and the local login can be reached by anyone. */
    static final int MIN_PASSWORD_LENGTH = 12;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRecordRepository users;

    private final AccessManager accessManager;

    private final PasswordEncoder passwordEncoder;

    /**
     * Users known to have a {@code Users} record, so that {@link #registerIfAbsent} costs a query only on a user's
     * first request after startup.
     */
    private final Cache<String, Boolean> registeredUsers = Caffeine.newBuilder()
                                                                   .maximumSize(10_000)
                                                                   .expireAfterWrite(Duration.ofHours(1))
                                                                   .build();

    public UserService(@Nonnull UserRecordRepository users,
                       @Nonnull AccessManager accessManager,
                       @Nonnull PasswordEncoder passwordEncoder) {
        this.users = checkNotNull(users);
        this.accessManager = checkNotNull(accessManager);
        this.passwordEncoder = checkNotNull(passwordEncoder);
    }

    // Authentication

    /**
     * Creates the {@code Users} record of a user seen for the first time in a token, with the name and e-mail address
     * that the token gives (docs/01 §6); an existing record is left as it is. A blank real name becomes the user name.
     *
     * @throws IllegalArgumentException for the guest user, who never has a record
     */
    public void registerIfAbsent(@Nonnull UserId userId, @Nullable String realName, @Nullable String emailAddress) {
        checkArgument(!userId.isGuest(), "The guest user cannot be registered");
        var userName = userId.getUserName();
        if (registeredUsers.getIfPresent(userName) != null) {
            return;
        }
        var name = realName == null || realName.isBlank() ? userName : realName;
        var email = emailAddress == null ? "" : emailAddress;
        if (users.insertIfAbsent(UserRecordDocument.of(userId, name, email, ""))) {
            logger.info("Registered user {} on first sign-in", userName);
        }
        registeredUsers.put(userName, Boolean.TRUE);
    }

    /**
     * The BCrypt hash of the user's local password, for the local fallback login.
     */
    @Nonnull
    public Optional<String> findLocalPasswordHash(@Nonnull UserId userId) {
        if (userId.isGuest()) {
            return Optional.empty();
        }
        return users.findOne(userId).map(UserRecordDocument::localPasswordHash);
    }

    // Command line

    /**
     * Makes the user an administrator ({@code wp-cli create-admin}, docs/01 §8): creates the {@code Users} record or
     * sets the e-mail address of an existing one, sets the local password if one is given, and adds
     * {@code SystemAdmin} to the user's application roles.
     * <p>
     * Unlike the legacy {@code SetupTools}, which replaced the user's application roles with {@code SystemAdmin}, the
     * other application roles of an existing user are kept, and an existing record keeps its name and legacy fields.
     *
     * @param password the local password, or {@code null} to leave the local password as it is
     * @throws IllegalArgumentException if the user name, e-mail address or password is not acceptable, or another
     *                                  user has the e-mail address
     */
    @Nonnull
    public AdministratorAccount createAdministrator(@Nonnull UserId userId,
                                                    @Nonnull String emailAddress,
                                                    @Nullable String password) {
        var userName = userId.getUserName();
        checkArgument(!userName.isBlank() && !userId.isGuest(), "'%s' cannot be a user name", userName);
        checkArgument(emailAddress.contains("@"), "'%s' is not an e-mail address", emailAddress);
        users.findOneByEmailAddress(emailAddress)
             .filter(other -> !other.getUserId().equals(userId))
             .ifPresent(other -> {
                 throw new IllegalArgumentException("The e-mail address " + emailAddress + " belongs to "
                                                            + other.userId());
             });
        var passwordHash = password == null ? null : passwordEncoder.encode(checkPassword(password));

        var existing = users.findOne(userId);
        var record = existing.map(user -> user.withEmailAddress(emailAddress))
                             .orElseGet(() -> UserRecordDocument.of(userId, userName, emailAddress, ""));
        if (passwordHash != null) {
            record = record.withLocalPasswordHash(passwordHash);
        }
        users.save(record);
        registeredUsers.put(userName, Boolean.TRUE);

        var application = ApplicationResource.get();
        var roles = new LinkedHashSet<RoleId>(accessManager.getAssignedRoles(Subject.forUser(userId), application));
        roles.add(BuiltInRole.SYSTEM_ADMIN.getRoleId());
        accessManager.setAssignedRoles(Subject.forUser(userId), application, roles);
        logger.info("User {} is an administrator", userName);
        return new AdministratorAccount(userId, existing.isEmpty(), passwordHash != null);
    }

    private static String checkPassword(String password) {
        checkArgument(password.length() >= MIN_PASSWORD_LENGTH,
                      "The password must have at least %s characters", MIN_PASSWORD_LENGTH);
        checkArgument(password.getBytes(StandardCharsets.UTF_8).length <= MAX_PASSWORD_BYTES,
                      "The password must not be longer than %s bytes", MAX_PASSWORD_BYTES);
        return password;
    }

    // Requests

    /**
     * The caller as the client shows it ({@code GET /api/v1/me}), with the actions the caller may perform on the
     * application; replaces the legacy {@code UserInSession}.
     */
    @Nonnull
    public CurrentUser getCurrentUser(@Nonnull UserId caller) {
        accessManager.requireSignedIn(caller);
        var profile = getUserProfile(caller).orElseGet(() -> new UserProfile(caller, caller.getUserName(), null));
        var actions = accessManager.getActionClosure(Subject.forUser(caller), ApplicationResource.get())
                                   .stream()
                                   .sorted(Comparator.comparing(ActionId::getId))
                                   .toList();
        return new CurrentUser(profile.userId(), profile.displayName(), profile.emailAddress(), actions);
    }

    /**
     * The user names that contain the text, ignoring case, sorted; for picking a user to share a project with. Every
     * signed-in user may ask, as in the legacy application.
     */
    @Nonnull
    public List<UserId> getUserIdCompletions(@Nonnull UserId caller, @Nonnull String text) {
        accessManager.requireSignedIn(caller);
        return users.findByUserIdContainingIgnoreCase(checkNotNull(text), COMPLETIONS_LIMIT)
                    .stream()
                    .sorted()
                    .toList();
    }

    /**
     * The users whose name contains the text, ignoring case, in name order ({@code GET /api/v1/admin/users}); at most
     * {@code limit}, capped at {@value #MAX_USERS}.
     */
    @Nonnull
    public List<UserProfile> findUsers(@Nonnull UserId caller, @Nonnull String text, int limit) {
        accessManager.require(caller, ApplicationResource.get(), BuiltInAction.VIEW_ANY_USER_DETAILS);
        var cappedLimit = Math.max(1, Math.min(limit, MAX_USERS));
        return users.findUsersContainingIgnoreCase(checkNotNull(text), cappedLimit)
                    .stream()
                    .map(UserService::toProfile)
                    .toList();
    }

    /**
     * Ported from the legacy {@code SetEmailAddressActionHandler}: users change only their own address, and an
     * address can belong to one user only.
     *
     * @throws PermissionDeniedException if the caller is not the user
     * @throws WpException                {@code USER_NOT_FOUND} if the user has no record (the legacy manager quietly
     *                                    did nothing)
     */
    @Nonnull
    public EmailAddressChange setEmailAddress(@Nonnull UserId caller,
                                              @Nonnull UserId userId,
                                              @Nonnull String emailAddress) {
        accessManager.requireSignedIn(caller);
        if (!caller.equals(userId)) {
            throw new PermissionDeniedException("Users can only change their own email addresses");
        }
        var owner = getUserIdByEmailAddress(emailAddress);
        if (owner.isPresent()) {
            return owner.get().equals(userId) ? EmailAddressChange.ADDRESS_UNCHANGED
                    : EmailAddressChange.ADDRESS_ALREADY_EXISTS;
        }
        var record = users.findOne(userId)
                          .orElseThrow(() -> WpException.notFound("USER_NOT_FOUND",
                                                                  "No such user: " + userId.getUserName()));
        users.save(record.withEmailAddress(emailAddress));
        logger.info("Email address for {} changed", userId.getUserName());
        return EmailAddressChange.ADDRESS_CHANGED;
    }

    // Other services

    /**
     * The user's name and e-mail address, if the user has a record; for the guest user always empty.
     */
    @Nonnull
    public Optional<UserProfile> getUserProfile(@Nonnull UserId userId) {
        if (userId.isGuest()) {
            return Optional.empty();
        }
        return users.findOne(userId).map(UserService::toProfile);
    }

    /**
     * The user's e-mail address, if it is known and not empty; as the legacy {@code UserDetailsManager.getEmail}.
     */
    @Nonnull
    public Optional<String> getEmailAddress(@Nonnull UserId userId) {
        return getUserProfile(userId).map(UserProfile::emailAddress);
    }

    /**
     * The user with the e-mail address; none for the empty address, as in the legacy manager.
     */
    @Nonnull
    public Optional<UserId> getUserIdByEmailAddress(@Nonnull String emailAddress) {
        if (emailAddress.isEmpty()) {
            return Optional.empty();
        }
        return users.findOneByEmailAddress(emailAddress).map(UserRecordDocument::getUserId);
    }

    private static UserProfile toProfile(UserRecordDocument user) {
        var displayName = user.realName().isBlank() ? user.userId() : user.realName();
        var emailAddress = user.emailAddress().isEmpty() ? null : user.emailAddress();
        return new UserProfile(user.getUserId(), displayName, emailAddress);
    }
}
