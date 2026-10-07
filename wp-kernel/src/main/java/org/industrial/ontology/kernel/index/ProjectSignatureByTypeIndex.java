package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ProjectSignatureByTypeIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-16
 */
public class ProjectSignatureByTypeIndex implements org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex, DependentIndex {

    @Nonnull
    private final AxiomsByEntityReferenceIndex delegate;

    public ProjectSignatureByTypeIndex(@Nonnull AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex) {
        this.delegate = checkNotNull(axiomsByEntityReferenceIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(delegate);
    }

    @Nonnull
    @Override
    public <E extends OWLEntity> Stream<E> getSignature(@Nonnull EntityType<E> entityType) {
        checkNotNull(entityType);
        return delegate.getProjectAxiomsSignature(entityType);
    }
}
