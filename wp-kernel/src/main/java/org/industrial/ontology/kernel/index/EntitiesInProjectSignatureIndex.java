package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureIndex;

import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EntitiesInProjectSignatureIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-17
 */
public class EntitiesInProjectSignatureIndex implements org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final EntitiesInOntologySignatureIndex entitiesInOntologySignatureIndex;

    public EntitiesInProjectSignatureIndex(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                               @Nonnull EntitiesInOntologySignatureIndex entitiesInOntologySignatureIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.entitiesInOntologySignatureIndex = checkNotNull(entitiesInOntologySignatureIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(projectOntologiesIndex, entitiesInOntologySignatureIndex);
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity entity) {
        checkNotNull(entity);
        return projectOntologiesIndex.getOntologyIds()
                .anyMatch(ontId -> entitiesInOntologySignatureIndex.containsEntityInSignature(entity, ontId));

    }
}
