package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureByIriIndex;

import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EntitiesInProjectSignatureByIriIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-13
 */
public class EntitiesInProjectSignatureByIriIndex implements org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final EntitiesInOntologySignatureByIriIndex entitiesInOntologySignatureByIriIndex;

    public EntitiesInProjectSignatureByIriIndex(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                                    @Nonnull EntitiesInOntologySignatureByIriIndex entitiesInOntologySignatureByIriIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.entitiesInOntologySignatureByIriIndex = checkNotNull(entitiesInOntologySignatureByIriIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(projectOntologiesIndex, entitiesInOntologySignatureByIriIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLEntity> getEntitiesInSignature(@Nonnull IRI entityIri) {
        checkNotNull(entityIri);
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> entitiesInOntologySignatureByIriIndex.getEntitiesInSignature(entityIri, ontId))
                                     .distinct();
    }
}
