package org.industrial.ontology.kernel.project;



import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

import java.util.UUID;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.DefaultOntologyIdManagerImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class DefaultOntologyIdManagerImpl implements DefaultOntologyIdManager, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    private OWLOntologyID freshOntologyId;

    public DefaultOntologyIdManagerImpl(ProjectOntologiesIndex projectOntologiesIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(projectOntologiesIndex);
    }

    @Nonnull
    @Override
    public synchronized OWLOntologyID getDefaultOntologyId() {
        Stream<OWLOntologyID> ontologyIds = projectOntologiesIndex.getOntologyIds();
        return ontologyIds.findFirst()
                .orElseGet(this::createFreshOntologyId);
    }

    private OWLOntologyID createFreshOntologyId() {
        if(freshOntologyId == null) {
            var ontologyIri = "urn:webprotege:ontology:" + UUID.randomUUID().toString();
            freshOntologyId = new OWLOntologyID(IRI.create(ontologyIri));
        }
        return freshOntologyId;
    }
}
