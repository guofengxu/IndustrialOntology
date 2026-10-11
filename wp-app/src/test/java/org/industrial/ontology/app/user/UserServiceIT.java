package org.industrial.ontology.app.user;

import org.bson.Document;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The user service on the {@code Users} and {@code RoleAssignments} collections. The e-mail tests include the legacy
 * {@code UserDetailsManagerImpl_TestCase}, converted.
 */
class UserServiceIT {

    private static final UserId CAROL = UserId.getUserId("carol");

    private static final UserId DAVE = UserId.getUserId("dave");

    private static MongoPersistenceTestContext context;

    private UserService service;

    private UserRecordRepository users;

    private AccessManager accessManager;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithServices();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        // A new service each time: the bean remembers which users it has registered.
        accessManager = context.bean(AccessManager.class);
        users = context.bean(UserRecordRepository.class);
        service = new UserService(users, accessManager, context.bean(PasswordEncoder.class));
    }

    private Document storedUser(UserId userId) {
        return context.database().getCollection(UserRecordDocument.COLLECTION)
                      .find(new Document("_id", userId.getUserName())).first();
    }

    @Test
    void shouldRegisterAUserSeenForTheFirstTime() {
        service.registerIfAbsent(CAROL, "Carol Danvers", "carol@example.org");

        assertThat(storedUser(CAROL)).isEqualTo(new Document("_id", "carol").append("realName", "Carol Danvers")
                                                                             .append("emailAddress",
                                                                                     "carol@example.org"));
    }

    @Test
    void shouldNotChangeAUserThatExists() {
        users.save(UserRecordDocument.of(CAROL, "Carol", "carol@old.example.org", "").withLocalPasswordHash("hash"));

        service.registerIfAbsent(CAROL, "Carol Danvers", "carol@example.org");

        assertThat(storedUser(CAROL)).containsEntry("realName", "Carol")
                                     .containsEntry("emailAddress", "carol@old.example.org")
                                     .containsEntry("localPasswordHash", "hash");
    }

    @Test
    void shouldUseTheUserNameWhenATokenHasNoName() {
        service.registerIfAbsent(CAROL, " ", null);

        assertThat(service.getUserProfile(CAROL)).contains(new UserProfile(CAROL, "carol", null));
    }

    @Test
    void shouldNotRegisterTheGuest() {
        assertThatThrownBy(() -> service.registerIfAbsent(UserId.getGuest(), "Guest", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void currentUserShouldHaveTheApplicationActionsOfItsRolesAndThoseOfAnySignedInUser() {
        service.registerIfAbsent(CAROL, "Carol Danvers", "carol@example.org");
        accessManager.setAssignedRoles(Subject.forAnySignedInUser(), ApplicationResource.get(),
                                       Set.of(BuiltInRole.PROJECT_CREATOR.getRoleId()));
        accessManager.setAssignedRoles(Subject.forUser(CAROL), ApplicationResource.get(),
                                       Set.of(BuiltInRole.PROJECT_UPLOADER.getRoleId()));

        var me = service.getCurrentUser(CAROL);

        assertThat(me.userId()).isEqualTo(CAROL);
        assertThat(me.displayName()).isEqualTo("Carol Danvers");
        assertThat(me.emailAddress()).isEqualTo("carol@example.org");
        assertThat(me.applicationActions()).extracting(ActionId::getId)
                                           .containsExactly("CreateEmptyProject", "UploadProject");
    }

    @Test
    void currentUserWithoutRecordShouldBeShownByName() {
        var me = service.getCurrentUser(DAVE);

        assertThat(me.displayName()).isEqualTo("dave");
        assertThat(me.emailAddress()).isNull();
        assertThat(me.applicationActions()).isEmpty();
        assertThatThrownBy(() -> service.getCurrentUser(UserId.getGuest()))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void createAdministratorShouldCreateTheUserWithSystemAdminAndAPassword() {
        var account = service.createAdministrator(CAROL, "carol@example.org", "correct horse");

        assertThat(account).isEqualTo(new AdministratorAccount(CAROL, true, true));
        assertThat(storedUser(CAROL)).containsEntry("realName", "carol")
                                     .containsEntry("emailAddress", "carol@example.org");
        var hash = service.findLocalPasswordHash(CAROL).orElseThrow();
        assertThat(hash).startsWith("$2a$");
        assertThat(context.bean(PasswordEncoder.class).matches("correct horse", hash)).isTrue();
        assertThat(accessManager.getActionClosure(Subject.forUser(CAROL), ApplicationResource.get()))
                .contains(BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId());
        assertThat(service.getCurrentUser(CAROL).applicationActions())
                .contains(BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId());
    }

    @Test
    void createAdministratorShouldKeepAnExistingUsersRecordAndRoles() {
        users.save(new UserRecordDocument("carol", "Carol Danvers", "carol@old.example.org", null, "salt", "digest",
                                          null));
        accessManager.setAssignedRoles(Subject.forUser(CAROL), ApplicationResource.get(),
                                       List.of(BuiltInRole.PROJECT_CREATOR.getRoleId()));

        var account = service.createAdministrator(CAROL, "carol@example.org", null);

        assertThat(account).isEqualTo(new AdministratorAccount(CAROL, false, false));
        assertThat(storedUser(CAROL)).containsEntry("realName", "Carol Danvers")
                                     .containsEntry("emailAddress", "carol@example.org")
                                     .containsEntry("salt", "salt")
                                     .doesNotContainKey("localPasswordHash");
        assertThat(accessManager.getAssignedRoles(Subject.forUser(CAROL), ApplicationResource.get()))
                .containsExactly(BuiltInRole.PROJECT_CREATOR.getRoleId(), BuiltInRole.SYSTEM_ADMIN.getRoleId());
    }

    @Test
    void createAdministratorShouldRejectUnacceptableInput() {
        users.save(UserRecordDocument.of(DAVE, "Dave", "dave@example.org", ""));

        assertThatThrownBy(() -> service.createAdministrator(CAROL, "dave@example.org", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("belongs to dave");
        assertThatThrownBy(() -> service.createAdministrator(CAROL, "not an address", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.createAdministrator(CAROL, "carol@example.org", "short"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 12");
        assertThatThrownBy(() -> service.createAdministrator(CAROL, "carol@example.org", "x".repeat(73)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("72 bytes");
        assertThatThrownBy(() -> service.createAdministrator(UserId.getGuest(), "guest@example.org", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(storedUser(CAROL)).isNull();
    }

    @Test
    void findUsersShouldRequireViewAnyUserDetails() {
        users.save(UserRecordDocument.of(CAROL, "Carol Danvers", "carol@example.org", ""));
        users.save(UserRecordDocument.of(UserId.getUserId("Caroline"), "", "", ""));
        users.save(UserRecordDocument.of(DAVE, "Dave", "dave@example.org", ""));

        assertThatThrownBy(() -> service.findUsers(DAVE, "car", 10)).isInstanceOf(PermissionDeniedException.class);

        accessManager.setAssignedRoles(Subject.forUser(DAVE), ApplicationResource.get(),
                                       List.of(BuiltInRole.USER_ADMIN.getRoleId()));
        assertThat(service.findUsers(DAVE, "CAR", 10)).containsExactly(
                new UserProfile(UserId.getUserId("Caroline"), "Caroline", null),
                new UserProfile(CAROL, "Carol Danvers", "carol@example.org"));
        assertThat(service.findUsers(DAVE, "car", 1)).hasSize(1);
    }

    @Test
    void userIdCompletionsShouldBeSortedAndLimited() {
        for (var i = 11; i >= 0; i--) {
            users.save(UserRecordDocument.of(UserId.getUserId("user" + (char) ('a' + i)), "", "", ""));
        }

        var completions = service.getUserIdCompletions(DAVE, "USER");

        assertThat(completions).hasSize(UserService.COMPLETIONS_LIMIT).isSorted();
    }

    @Test
    void shouldGetEmailAndUserIdByEmail() {
        users.save(UserRecordDocument.of(CAROL, "Carol", "carol@example.org", ""));
        users.save(UserRecordDocument.of(DAVE, "Dave", "", ""));

        assertThat(service.getEmailAddress(CAROL)).contains("carol@example.org");
        assertThat(service.getEmailAddress(DAVE)).isEmpty();
        assertThat(service.getEmailAddress(UserId.getGuest())).isEmpty();
        assertThat(service.getUserIdByEmailAddress("carol@example.org")).contains(CAROL);
        assertThat(service.getUserIdByEmailAddress("")).isEmpty();
    }

    @Test
    void setEmailAddressShouldFollowTheLegacyRules() {
        users.save(UserRecordDocument.of(CAROL, "Carol", "carol@example.org", ""));
        users.save(UserRecordDocument.of(DAVE, "Dave", "dave@example.org", ""));

        assertThatThrownBy(() -> service.setEmailAddress(DAVE, CAROL, "new@example.org"))
                .isInstanceOf(PermissionDeniedException.class);
        assertThat(service.setEmailAddress(CAROL, CAROL, "carol@example.org"))
                .isEqualTo(EmailAddressChange.ADDRESS_UNCHANGED);
        assertThat(service.setEmailAddress(CAROL, CAROL, "dave@example.org"))
                .isEqualTo(EmailAddressChange.ADDRESS_ALREADY_EXISTS);
        assertThat(service.setEmailAddress(CAROL, CAROL, "carol@new.example.org"))
                .isEqualTo(EmailAddressChange.ADDRESS_CHANGED);
        assertThat(service.getEmailAddress(CAROL)).contains("carol@new.example.org");

        var nobody = UserId.getUserId("nobody");
        assertThatThrownBy(() -> service.setEmailAddress(nobody, nobody, "nobody@example.org"))
                .isInstanceOfSatisfying(WpException.class,
                                        e -> assertThat(e.getCode()).isEqualTo("USER_NOT_FOUND"));
    }
}
