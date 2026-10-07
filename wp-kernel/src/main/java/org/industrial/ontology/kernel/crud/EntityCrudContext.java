package org.industrial.ontology.kernel.crud;



import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudContext}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 08/08/2013
 */
public class EntityCrudContext {

    private final ProjectId projectId;

    private final PrefixedNameExpander prefixedNameExpander;

    private final UserId userId;

    @Nonnull
    private final ProjectDetailsRepository projectDetailsRepository;

    @Nullable
    private DictionaryLanguage dictionaryLanguage;

    @Nonnull
    private OWLOntologyID targetOntologyId;

    public EntityCrudContext(@Nonnull ProjectId projectId,
                             @Nonnull UserId userId,
                             @Nonnull PrefixedNameExpander prefixedNameExpander,
                             @Nonnull ProjectDetailsRepository projectDetailsRepository,
                             @Nonnull OWLOntologyID targetOntologyId) {
        this.projectId = checkNotNull(projectId);
        this.userId = checkNotNull(userId);
        this.prefixedNameExpander = checkNotNull(prefixedNameExpander);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.targetOntologyId = checkNotNull(targetOntologyId);
    }

    @Nonnull
    public UserId getUserId() {
        return userId;
    }

    @Nonnull
    public PrefixedNameExpander getPrefixedNameExpander() {
        return prefixedNameExpander;
    }

    @Nonnull
    public OWLOntologyID getTargetOntologyId() {
        return targetOntologyId;
    }

    @Nonnull
    public DictionaryLanguage getDictionaryLanguage() {
        if(dictionaryLanguage == null) {
            dictionaryLanguage = projectDetailsRepository.findOne(projectId)
                                                                .map(ProjectDetails::getDefaultDictionaryLanguage)
                                                                .orElse(DictionaryLanguage.rdfsLabel(""));
        }
        return dictionaryLanguage;
    }
}
