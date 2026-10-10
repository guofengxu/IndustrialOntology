package org.industrial.ontology.app.project;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;

/**
 * A request names a project that has no {@code ProjectDetails}: 404 {@code PROJECT_NOT_FOUND} (docs/02 §1). The
 * legacy {@code UnknownProjectException}.
 * <p>
 * Services check that the project exists before they check the caller's permissions on it, so an unknown project is
 * 404 for everyone. Project ids are random UUIDs, so this tells a caller nothing that guessing could find.
 */
public class ProjectNotFoundException extends WpException {

    public static final String CODE = "PROJECT_NOT_FOUND";

    public ProjectNotFoundException(@Nonnull ProjectId projectId) {
        super(CODE, 404, "Project " + projectId.getId() + " does not exist");
    }
}
