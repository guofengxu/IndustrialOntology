package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The {@code CREATE_*} checks that the legacy {@code ChangeManager} made for fresh entities.
 */
class AccessChangePermissionCheckerTest {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final UserId USER = UserId.getUserId("bob");

    private static final Map<EntityType<?>, BuiltInAction> ACTIONS = Map.of(
            EntityType.CLASS, BuiltInAction.CREATE_CLASS,
            EntityType.OBJECT_PROPERTY, BuiltInAction.CREATE_PROPERTY,
            EntityType.DATA_PROPERTY, BuiltInAction.CREATE_PROPERTY,
            EntityType.ANNOTATION_PROPERTY, BuiltInAction.CREATE_PROPERTY,
            EntityType.NAMED_INDIVIDUAL, BuiltInAction.CREATE_INDIVIDUAL,
            EntityType.DATATYPE, BuiltInAction.CREATE_DATATYPE);

    @Test
    void shouldRequireTheCreateActionOfEachEntityTypeOnTheProject() {
        ACTIONS.forEach((entityType, action) -> {
            var checker = new AccessChangePermissionChecker(allowingOnly(action), PROJECT);

            assertThatCode(() -> checker.checkCreatePermission(USER, entityType)).as("%s", entityType)
                                                                              .doesNotThrowAnyException();
        });
    }

    @Test
    void shouldDenyWithTheLegacyMessages() {
        var checker = new AccessChangePermissionChecker(allowingOnly(BuiltInAction.EDIT_ONTOLOGY), PROJECT);

        assertThatThrownBy(() -> checker.checkCreatePermission(USER, EntityType.CLASS))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("You do not have permission to create new classes");
        assertThatThrownBy(() -> checker.checkCreatePermission(USER, EntityType.ANNOTATION_PROPERTY))
                .hasMessage("You do not have permission to create new properties");
        assertThatThrownBy(() -> checker.checkCreatePermission(USER, EntityType.NAMED_INDIVIDUAL))
                .hasMessage("You do not have permission to create new individuals");
        assertThatThrownBy(() -> checker.checkCreatePermission(USER, EntityType.DATATYPE))
                .hasMessage("You do not have permission to create new datatypes");
    }

    private static AccessManager allowingOnly(BuiltInAction allowed) {
        var accessManager = mock(AccessManager.class);
        when(accessManager.hasPermission(any(Subject.class), any(Resource.class), any(BuiltInAction.class)))
                .thenReturn(false);
        when(accessManager.hasPermission(eq(Subject.forUser(USER)), eq(ProjectResource.of(PROJECT)), eq(allowed)))
                .thenReturn(true);
        return accessManager;
    }
}
