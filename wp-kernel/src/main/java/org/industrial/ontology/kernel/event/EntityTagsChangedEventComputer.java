package org.industrial.ontology.kernel.event;



import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.OntologyChangeSubjectProvider;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.port.TagsManager;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.event.EntityTagsChangedEvent;
import org.industrial.ontology.domain.tag.Tag;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.EntityTagsChangedEventComputer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 Jun 2018
 */
public class EntityTagsChangedEventComputer implements EventTranslator {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final OntologyChangeSubjectProvider provider;

    @Nonnull
    private final TagsManager tagsManager;

    private final SetMultimap<OWLEntity, Tag> beforeChangesTags = HashMultimap.create();

    public EntityTagsChangedEventComputer(@Nonnull ProjectId projectId, @Nonnull OntologyChangeSubjectProvider provider,
                                          @Nonnull TagsManager tagsManager) {
        this.projectId = checkNotNull(projectId);
        this.provider = checkNotNull(provider);
        this.tagsManager = checkNotNull(tagsManager);
    }

    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
        submittedChanges.forEach(chg -> {
            provider.getChangeSubjects(chg).forEach(entity -> {
                beforeChangesTags.putAll(entity, tagsManager.getTags(entity));
            });
        });
    }

    @Override
    public void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> changes, List<ProjectEvent> projectEventList) {
        changes.getChangeList().forEach(chg -> {
            provider.getChangeSubjects(chg).forEach(entity -> {
                Collection<Tag> tags = tagsManager.getTags(entity);
                if(!tags.equals(beforeChangesTags.get(entity))) {
                    projectEventList.add(new EntityTagsChangedEvent(projectId, entity, tags));
                }
            });
        });
    }
}
