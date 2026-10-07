package org.industrial.ontology.domain.core;

import org.industrial.ontology.domain.util.UUIDUtil;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.ProjectId_TestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/20/13
 */
public class ProjectIdTest {

    @Test
    public void equalsShouldReturnTrueForObjectsWithSameId() {
        String uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        ProjectId projectIdA = ProjectId.get(uuid);
        ProjectId projectIdB = ProjectId.get(uuid);
        assertEquals(projectIdA, projectIdB);
    }

    @Test
    public void equalsShouldReturnFalseForObjectsWithDifferentIds() {
        String uuidA = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        String uuidB = "d16a6ca0-3afc-4af3-8a95-82cdc82f52cc";
        ProjectId projectIdA = ProjectId.get(uuidA);
        ProjectId projectIdB = ProjectId.get(uuidB);
        assertFalse(projectIdA.equals(projectIdB));
    }

    @Test
    public void equalsNullReturnsFalse() {
        String uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        ProjectId projectId = ProjectId.get(uuid);
        assertNotNull(projectId);
    }

    @Test
    public void malformedUUIDThrowsProjectIdFormatException() {
        assertThrows(ProjectIdFormatException.class, () -> {
            String malformedId = "wrong";
            ProjectId.get(malformedId);
        });
    }

    @Test
    public void getIdReturnsSuppliedValue() {
        String uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        ProjectId projectId = ProjectId.get(uuid);
        assertEquals(uuid, projectId.getId());
    }

    @Test
    public void getRegExpReturnsCorrectExpression() {
        String regExp = UUIDUtil.getIdRegExp().pattern();
        assertEquals("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", regExp);
    }

    @Test
    public void getFromNullableReturnsAbsentWithNull() {
        Optional<ProjectId> projectIdOptional = ProjectId.getFromNullable(null);
        assertFalse(projectIdOptional.isPresent());
    }

    @Test
    public void getFromNullableReturnsProjectIdWithNonNull() {
        String uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        Optional<ProjectId> projectIdOptional = ProjectId.getFromNullable(uuid);
        assertTrue(projectIdOptional.isPresent());
    }

    @Test
    public void getFromNullableThrowsProjectIdFormatExceptionForMalformedId() {
        assertThrows(ProjectIdFormatException.class, () -> {
            String malformedId = "wrong";
            ProjectId.getFromNullable(malformedId);
        });
    }

    @Test
    public void getThrowsNullPointerForNullArgument() {
        assertThrows(NullPointerException.class, () -> {
            ProjectId.get(null);
        });
    }
}
