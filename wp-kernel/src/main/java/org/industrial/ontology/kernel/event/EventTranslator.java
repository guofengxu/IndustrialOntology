package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.domain.event.ProjectEvent;

import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.EventTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22/05/15
 */
public interface EventTranslator {

    /**
     * Prepare this translator for the following ontology changes.
     * @param submittedChanges The list of ontology changes to be submitted.  The actual changes that will be applied
     *                         will either be this list or a subset of this list.
     */
    void prepareForOntologyChanges(List<OntologyChange> submittedChanges);

    /**
     * Translate the ontology changes that were applied to high level project events.
     * @param revision The revision
     * @param changes The applied changes.
     * @param projectEventList A list to be filled with high level project events that were generated from the changes.
     */
    void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> changes, List<ProjectEvent> projectEventList);
}
