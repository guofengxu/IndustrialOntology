package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Converted from the legacy {@code Subject_AnySignedInUser_TestCase}, {@code Subject_GuestUser_TestCase} and
 * {@code Subject_SpecificUser_TestCase}.
 */
class SubjectTest {

    @Nested
    class AnySignedInUser {

        private final Subject subject = Subject.forAnySignedInUser();

        @Test
        void shouldReturnFalseForIsGuest() {
            assertThat(subject.isGuest()).isFalse();
        }

        @Test
        void shouldReturnEmptyUserName() {
            assertThat(subject.getUserName()).isEmpty();
            assertThat(subject.getUserId()).isEmpty();
        }

        @Test
        void shouldReturnTrueForIsAnySignedInUser() {
            assertThat(subject.isAnySignedInUser()).isTrue();
        }

        @Test
        void shouldEqualOtherAnySignedInUserSubject() {
            assertThat(subject).isEqualTo(Subject.forAnySignedInUser()).hasSameHashCodeAs(Subject.forAnySignedInUser());
        }

        @Test
        void shouldNotBeEqualToGuestUser() {
            assertThat(subject).isNotEqualTo(Subject.forGuestUser());
        }

        @Test
        void shouldNotBeEqualToSpecificUser() {
            assertThat(subject).isNotEqualTo(Subject.forUser("Other User"));
        }
    }

    @Nested
    class GuestUser {

        private final Subject subject = Subject.forGuestUser();

        @Test
        void shouldReturnTrueForIsGuest() {
            assertThat(subject.isGuest()).isTrue();
        }

        @Test
        void shouldReturnGuestUserName() {
            assertThat(subject.getUserName()).isEqualTo(Optional.of(UserId.getGuest().getUserName()));
        }

        @Test
        void shouldReturnFalseForIsAnySignedInUser() {
            assertThat(subject.isAnySignedInUser()).isFalse();
        }

        @Test
        void shouldEqualOtherGuestSubject() {
            assertThat(subject).isEqualTo(Subject.forGuestUser());
        }

        @Test
        void shouldBeTheSubjectOfTheNullUserName() {
            assertThat(Subject.forUser((String) null)).isEqualTo(subject);
        }

        @Test
        void shouldNotBeEqualToAnySignedInUser() {
            assertThat(subject).isNotEqualTo(Subject.forAnySignedInUser());
        }

        @Test
        void shouldNotBeEqualToSpecificUser() {
            assertThat(subject).isNotEqualTo(Subject.forUser("Other User"));
        }
    }

    @Nested
    class SpecificUser {

        private static final String THE_USER = "TheUser";

        private final Subject subject = Subject.forUser(THE_USER);

        @Test
        void shouldReturnFalseForIsGuest() {
            assertThat(subject.isGuest()).isFalse();
        }

        @Test
        void shouldReturnUserName() {
            assertThat(subject.getUserName()).contains(THE_USER);
            assertThat(subject.getUserId()).contains(UserId.getUserId(THE_USER));
        }

        @Test
        void shouldReturnFalseForIsAnySignedInUser() {
            assertThat(subject.isAnySignedInUser()).isFalse();
        }

        @Test
        void shouldEqualOtherUser() {
            assertThat(subject).isEqualTo(Subject.forUser(THE_USER))
                               .isEqualTo(Subject.forUser(UserId.getUserId(THE_USER)))
                               .hasSameHashCodeAs(Subject.forUser(THE_USER));
        }

        @Test
        void shouldNotBeEqualToAnySignedInUser() {
            assertThat(subject).isNotEqualTo(Subject.forAnySignedInUser());
        }

        @Test
        void shouldNotBeEqualToGuestUser() {
            assertThat(subject).isNotEqualTo(Subject.forGuestUser());
        }

        @Test
        void shouldThrowNullPointerExceptionIfUserIdIsNull() {
            assertThatThrownBy(() -> Subject.forUser((UserId) null)).isInstanceOf(NullPointerException.class);
        }
    }
}
