package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.domain.event.ProjectEvent;

import java.util.Collection;
import java.util.List;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.EventTranslatorManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22/05/15
 */
public class EventTranslatorManager {

    private Collection<EventTranslator> eventTranslators;

    public EventTranslatorManager(Set<EventTranslator> eventTranslators) {
        this.eventTranslators = eventTranslators;
    }

    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
        for(EventTranslator eventTranslator : eventTranslators) {
            eventTranslator.prepareForOntologyChanges(submittedChanges);
        }
    }

    public void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> appliedChanges, List<ProjectEvent> projectEventList) {
        for(EventTranslator eventTranslator : eventTranslators) {
            eventTranslator.translateOntologyChanges(revision, appliedChanges, projectEventList);
        }
    }
}
