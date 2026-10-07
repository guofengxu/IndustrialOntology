package org.industrial.ontology.kernel.event;



import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.HasGetChangeSubjects;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.domain.event.BrowserTextChangedEvent;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.HasContainsEntityInSignature;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.BrowserTextChangedEventComputer}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
public class BrowserTextChangedEventComputer implements EventTranslator {

    private final Map<OWLEntity, ImmutableMap<DictionaryLanguage, String>> shortFormMap = Maps.newHashMap();

    private final ProjectId projectId;

    @Nonnull
    private final DictionaryManager dictionaryManager;

    private final HasGetChangeSubjects changeSubjectsProvider;

    private final HasContainsEntityInSignature signature;

    public BrowserTextChangedEventComputer(@Nonnull ProjectId projectId,
                                           @Nonnull DictionaryManager dictionaryManager,
                                           @Nonnull HasGetChangeSubjects changeSubjectsProvider,
                                           @Nonnull HasContainsEntityInSignature signature) {
        this.projectId = checkNotNull(projectId);
        this.dictionaryManager = checkNotNull(dictionaryManager);
        this.changeSubjectsProvider = checkNotNull(changeSubjectsProvider);
        this.signature = checkNotNull(signature);
    }

    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
        shortFormMap.clear();
        submittedChanges.stream()
                        .flatMap(change -> changeSubjectsProvider.getChangeSubjects(change).stream())
                        // The changes might only be extending the signature, so check for this.
                        // If this is the case, then there will not be any existing short form.
                        .filter(signature::containsEntityInSignature)
                        .forEach(entity -> {
                            ImmutableMap<DictionaryLanguage, String> shortForms = dictionaryManager.getShortForms(entity);
                            shortFormMap.put(entity, shortForms);
                        });
    }

    @Override
    public void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> changes, List<ProjectEvent> projectEventList) {
        Set<OWLEntity> processedEntities = new HashSet<>();
        changes.getChangeList().stream()
               .flatMap(change -> changeSubjectsProvider.getChangeSubjects(change).stream())
               .distinct()
               .forEach(entity -> {
                   ImmutableMap<DictionaryLanguage, String> oldShortForms = shortFormMap.get(entity);
                   ImmutableMap<DictionaryLanguage, String> shortForms = dictionaryManager.getShortForms(entity);
                   if(oldShortForms == null || !shortForms.equals(oldShortForms)) {
                       projectEventList.add(new BrowserTextChangedEvent(entity, dictionaryManager.getShortForm(entity), projectId, dictionaryManager.getShortForms(entity)));
                   }
               });
    }
}
