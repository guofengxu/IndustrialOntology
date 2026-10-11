package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Converted from the legacy {@code ApplicationResource_TestCase} and {@code ProjectResource_TestCase}.
 */
class ResourceTest {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    @Nested
    class Application {

        private final ApplicationResource resource = ApplicationResource.get();

        @Test
        void shouldBeEqualToOther() {
            assertThat(resource).isEqualTo(ApplicationResource.get()).hasSameHashCodeAs(ApplicationResource.get());
        }

        @Test
        @SuppressWarnings("ConstantConditions")
        void shouldNotBeEqualToNull() {
            assertThat(resource.equals(null)).isFalse();
        }

        @Test
        void shouldImplementToString() {
            assertThat(resource.toString()).startsWith("ApplicationResource");
        }

        @Test
        void shouldHaveNoProjectId() {
            assertThat(resource.getProjectId()).isEmpty();
        }

        @Test
        void shouldBeTheApplication() {
            assertThat(resource.isApplication()).isTrue();
            assertThat(resource.isProject()).isFalse();
            assertThat(resource.isProject(PROJECT)).isFalse();
        }

        @Test
        void shouldNotBeEqualToAProject() {
            assertThat((Resource) resource).isNotEqualTo(ProjectResource.of(PROJECT));
        }
    }

    @Nested
    class Project {

        private final ProjectResource resource = ProjectResource.of(PROJECT);

        @Test
        void shouldThrowNullPointerExceptionIfProjectIdIsNull() {
            assertThatThrownBy(() -> new ProjectResource(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void shouldReturnSuppliedProjectId() {
            assertThat(resource.getProjectId()).contains(PROJECT);
        }

        @Test
        void shouldBeEqualToOther() {
            assertThat(resource).isEqualTo(ProjectResource.forProject(PROJECT))
                                .hasSameHashCodeAs(ProjectResource.forProject(PROJECT));
        }

        @Test
        void shouldNotBeEqualToOtherThatHasDifferentProjectId() {
            assertThat(resource).isNotEqualTo(ProjectResource.of(OTHER_PROJECT));
        }

        @Test
        void shouldImplementToString() {
            assertThat(resource.toString()).startsWith("ProjectResource");
        }

        @Test
        void shouldBeItsProjectOnly() {
            assertThat(resource.isProject()).isTrue();
            assertThat(resource.isProject(PROJECT)).isTrue();
            assertThat(resource.isProject(OTHER_PROJECT)).isFalse();
            assertThat(resource.isApplication()).isFalse();
        }
    }
}
