package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.project.PizzaOntology;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_CREATOR;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_UPLOADER;

/**
 * Creates users' roles and projects for the S6 service tests, through the services of a
 * {@link MongoPersistenceTestContext#startWithProjects} context.
 */
public final class ProjectTestFixture {

    private final MongoPersistenceTestContext context;

    public ProjectTestFixture(MongoPersistenceTestContext context) {
        this.context = context;
    }

    /**
     * Replaces the user's application roles.
     */
    public void grantApplicationRoles(UserId user, BuiltInRole... roles) {
        accessManager().setAssignedRoles(Subject.forUser(user),
                                         ApplicationResource.get(),
                                         Stream.of(roles).map(BuiltInRole::getRoleId).toList());
    }

    /**
     * Replaces the user's roles on the project; no roles removes them.
     */
    public void grantProjectRoles(UserId user, ProjectId projectId, BuiltInRole... roles) {
        accessManager().setAssignedRoles(Subject.forUser(user),
                                         ProjectResource.of(projectId),
                                         Stream.of(roles).map(BuiltInRole::getRoleId).toList());
    }

    /**
     * An empty project owned by the user, who is given the project creator role for it.
     */
    public ProjectId createProject(UserId owner, String displayName) {
        grantApplicationRoles(owner, PROJECT_CREATOR);
        return context.bean(ProjectService.class).createProject(owner, displayName, "", "en", null).getProjectId();
    }

    /**
     * A project owned by the user, created from an upload of the test pizza ontology, which has labels in English
     * and Chinese.
     */
    public ProjectId createPizzaProject(UserId owner, Path sources) {
        grantApplicationRoles(owner, PROJECT_CREATOR, PROJECT_UPLOADER);
        try {
            var documentId = upload(owner, PizzaOntology.copyTo(Files.createTempDirectory(sources, "pizza")));
            return context.bean(ProjectService.class).createProject(owner, "Pizza", "", "en", documentId)
                          .getProjectId();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DocumentId upload(UserId user, Path file) throws IOException {
        try (var content = Files.newInputStream(file)) {
            return context.bean(UploadService.class).upload(user, file.getFileName().toString(), content)
                          .documentId();
        }
    }

    private AccessManager accessManager() {
        return context.bean(AccessManager.class);
    }
}
