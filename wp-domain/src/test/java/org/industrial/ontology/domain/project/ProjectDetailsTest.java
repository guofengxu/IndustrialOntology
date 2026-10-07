package org.industrial.ontology.domain.project;

import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.ProjectDetailsTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 17/10/2013
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectDetailsTest {

    public static final boolean IN_TRASH = true;

    @Mock
    private ProjectId projectId;

    @Mock
    private UserId owner;

    private long createdAt = 22L;

    @Mock
    private UserId createdBy;

    private long modifiedAt = 33L;

    @Mock
    private UserId modifiedBy;

    private String displayName;

    private String description;

    private ProjectDetails projectDetails;

    @BeforeEach
    public void setUp() throws Exception {
        displayName = "DisplayName";
        description = "Description";
        projectDetails = ProjectDetails.get(projectId, displayName, description, owner, IN_TRASH, DictionaryLanguage.rdfsLabel(""), DisplayNameSettings.empty(), createdAt, createdBy, modifiedAt, modifiedBy);
    }

    @Test
    public void emptyDisplayNameInConstructorIsOK() {
        assertEquals(projectDetails.getDisplayName(), displayName);
    }

    @Test
    public void emptyDescriptionInConstructorIsOK() {
        assertEquals(projectDetails.getDescription(), description);
    }

    @Test
    public void suppliedProjectIdIsReturnedByAccessor() {
        assertEquals(projectDetails.getProjectId(), projectId);
    }

    @Test
    public void suppliedUserIdIsReturnedByAccessor() {
        assertEquals(projectDetails.getOwner(), owner);
    }

    @Test
    public void suppliedTrashValueIsReturnedByAccessor() {
        assertEquals(projectDetails.isInTrash(), IN_TRASH);
    }
}
