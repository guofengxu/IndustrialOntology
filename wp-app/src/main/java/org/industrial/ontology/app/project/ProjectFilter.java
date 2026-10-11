package org.industrial.ontology.app.project;

import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.project.ProjectDetails;

import javax.annotation.Nonnull;

/**
 * The project lists of the legacy project manager ({@code ProjectManagerPresenter}'s view filters), which the client
 * computed from the available projects.
 */
public enum ProjectFilter {

    /** Projects that the user owns, not in the trash. */
    OWNED,

    /** Projects that others own and that the user may view, not in the trash. */
    SHARED,

    /** Projects that the user owns, in the trash. */
    TRASH;

    public boolean accepts(@Nonnull ProjectDetails details, @Nonnull UserId user) {
        var owned = details.getOwner().equals(user);
        return switch (this) {
            case OWNED -> !details.isInTrash() && owned;
            case SHARED -> !details.isInTrash() && !owned;
            case TRASH -> details.isInTrash() && owned;
        };
    }
}
