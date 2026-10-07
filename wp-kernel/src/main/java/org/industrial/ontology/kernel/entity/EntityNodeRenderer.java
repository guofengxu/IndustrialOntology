package org.industrial.ontology.kernel.entity;



import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.shortform.LanguageManager;
import org.industrial.ontology.kernel.mansyntax.render.DeprecatedEntityChecker;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.kernel.api.port.TagsManager;
import org.industrial.ontology.kernel.api.port.WatchManager;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.entity.EntityNodeRenderer}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 29 Nov 2017
 */
public class EntityNodeRenderer {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final DictionaryManager dictionaryManager;

    @Nonnull
    private final DeprecatedEntityChecker deprecatedEntityChecker;

    @Nonnull
    private final WatchManager watchManager;

    @Nonnull
    private final EntityDiscussionThreadRepository discussionThreadRepository;

    @Nonnull
    private final TagsManager tagsManager;

    @Nonnull
    private final LanguageManager languageManager;

    public EntityNodeRenderer(@Nonnull ProjectId projectId,
                              @Nonnull DictionaryManager dictionaryManager,
                              @Nonnull DeprecatedEntityChecker deprecatedEntityChecker,
                              @Nonnull WatchManager watchManager,
                              @Nonnull EntityDiscussionThreadRepository discussionThreadRepository,
                              @Nonnull TagsManager tagsManager, @Nonnull LanguageManager languageManager) {
        this.projectId = checkNotNull(projectId);
        this.dictionaryManager = checkNotNull(dictionaryManager);
        this.deprecatedEntityChecker = checkNotNull(deprecatedEntityChecker);
        this.watchManager = checkNotNull(watchManager);
        this.discussionThreadRepository = checkNotNull(discussionThreadRepository);
        this.tagsManager = checkNotNull(tagsManager);
        this.languageManager = checkNotNull(languageManager);
    }

    /**
     * Renders the node for the specified entity.
     * @param entity The entity to be rendered.
     * @return The node for the specified entity.
     */
    @Nonnull
    public EntityNode render(@Nonnull OWLEntity entity) {
        return EntityNode.get(
                entity,
                dictionaryManager.getShortForm(entity, languageManager.getLanguages()),
                dictionaryManager.getShortForms(entity),
                deprecatedEntityChecker.isDeprecated(entity),
                watchManager.getDirectWatches(entity),
                discussionThreadRepository.getOpenCommentsCount(projectId, entity),
                tagsManager.getTags(entity));
    }
}
