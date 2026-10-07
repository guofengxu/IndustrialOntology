package org.industrial.ontology.domain.change;



import org.industrial.ontology.domain.pagination.Page;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.change.HasProjectChanges}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Oct 2018
 */
public interface HasProjectChanges {

    @Nonnull
    Page<ProjectChange> getProjectChanges();
}
