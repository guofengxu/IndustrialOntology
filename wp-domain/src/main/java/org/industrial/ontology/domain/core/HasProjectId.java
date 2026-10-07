package org.industrial.ontology.domain.core;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.HasProjectId}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/01/2013
 */
public interface HasProjectId {

    /**
     * Get the {@link ProjectId}.
     * @return The {@link ProjectId}.  Not {@code null}.
     */
    @Nonnull
    ProjectId getProjectId();
}
