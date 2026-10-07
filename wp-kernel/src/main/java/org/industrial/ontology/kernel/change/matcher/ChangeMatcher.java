package org.industrial.ontology.kernel.change.matcher;



import org.industrial.ontology.kernel.api.change.OntologyChange;

import java.util.List;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.ChangeMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public interface ChangeMatcher {

    Optional<ChangeSummary> getDescription(List<OntologyChange> changeData);
}
