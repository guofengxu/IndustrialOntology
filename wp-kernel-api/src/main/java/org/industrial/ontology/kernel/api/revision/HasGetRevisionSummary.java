package org.industrial.ontology.kernel.api.revision;



import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;

import java.util.Optional;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.HasGetRevisionSummary}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22/05/15
 */
public interface HasGetRevisionSummary {

    Optional<RevisionSummary> getRevisionSummary(RevisionNumber revisionNumber);
}
