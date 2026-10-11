package org.industrial.ontology.app.access.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A document of the {@code RoleAssignments} collection, as Morphia wrote the legacy {@code RoleAssignment}: the roles
 * assigned to a subject on a resource, with their closures precomputed so that a permission check is one query.
 * <p>
 * A missing {@code userName} is the "any signed-in user" subject and a missing {@code projectId} is the application
 * resource; Morphia left null fields out and so does Spring Data. The closures are stored as the legacy access
 * manager computed them, duplicates included.
 *
 * @param id            server-generated; {@code null} for an assignment that has not been stored yet
 * @param assignedRoles role ids
 * @param roleClosure   the assigned roles and the roles they include
 * @param actionClosure the action ids of {@code roleClosure}, sorted
 */
@Document(RoleAssignmentDocument.COLLECTION)
public record RoleAssignmentDocument(@Id @Nullable ObjectId id,
                                     @Nullable String userName,
                                     @Nullable String projectId,
                                     @Nonnull List<String> assignedRoles,
                                     @Nonnull List<String> roleClosure,
                                     @Nonnull List<String> actionClosure) {

    public static final String COLLECTION = "RoleAssignments";

    public static final String USER_NAME = "userName";

    public static final String PROJECT_ID = "projectId";

    public static final String ROLE_CLOSURE = "roleClosure";

    public static final String ACTION_CLOSURE = "actionClosure";

    public RoleAssignmentDocument {
        assignedRoles = copyOf(assignedRoles);
        roleClosure = copyOf(roleClosure);
        actionClosure = copyOf(actionClosure);
    }

    private static List<String> copyOf(List<String> values) {
        return values == null ? List.of() : List.copyOf(Objects.requireNonNull(values));
    }
}
