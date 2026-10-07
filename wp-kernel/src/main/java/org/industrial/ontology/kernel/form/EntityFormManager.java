package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.industrial.ontology.domain.form.EntityFormSelector;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.industrial.ontology.kernel.api.port.EntityFormRepository;
import org.industrial.ontology.kernel.api.port.EntityFormSelectorRepository;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityFormManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-01
 */
public class EntityFormManager {

    @Nonnull
    private final EntityFormRepository entityFormRepository;

    @Nonnull
    private final EntityFormSelectorRepository entityFormSelectorRepository;

    @Nonnull
    private final MatchingEngine matchingEngine;

    public EntityFormManager(@Nonnull EntityFormRepository entityFormRepository,
                             @Nonnull EntityFormSelectorRepository entityFormSelectorRepository,
                             @Nonnull MatchingEngine matchingEngine) {
        this.entityFormRepository = entityFormRepository;
        this.entityFormSelectorRepository = entityFormSelectorRepository;
        this.matchingEngine = matchingEngine;
    }

    public ImmutableList<FormDescriptor> getFormDescriptors(@Nonnull OWLEntity entity,
                                                           @Nonnull ProjectId projectId) {
        var formIds = entityFormSelectorRepository.findFormSelectors(projectId)
                                    .filter(selector -> matchingEngine.matches(entity,
                                                                               selector.getCriteria()))
                                    .map(EntityFormSelector::getFormId)
                                    .collect(toImmutableSet());
        return entityFormRepository.findFormDescriptors(formIds, projectId)
                            .collect(toImmutableList());
    }

}
