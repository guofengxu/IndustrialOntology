package org.industrial.ontology.kernel.index;



import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ProjectOntologiesIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
public class ProjectOntologiesIndex implements org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex, UpdatableIndex {

    private static Logger logger = LoggerFactory.getLogger(ProjectOntologiesIndex.class);

    @Nonnull
    private final Multiset<OWLOntologyID> ontologyIds = HashMultiset.create();

    @Nonnull
    private ImmutableList<OWLOntologyID> cache = ImmutableList.of();

    private boolean initialized = false;

    public ProjectOntologiesIndex() {
    }

    @Nonnull
    @Override
    public synchronized Stream<OWLOntologyID> getOntologyIds() {
        if(!initialized) {
            throw new RuntimeException("Index not initialized");
        }
        return cache.stream();
    }

    public synchronized void init(RevisionManager revisionManager) {
        if(initialized) {
            return;
        }
        revisionManager.getRevisions()
                       .forEach(rev -> applyChanges(rev.getChanges()));
        initialized = true;
    }

    @Override
    public synchronized void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        for(var ontologyChange : changes) {
            if(ontologyChange.isAddAxiom() || ontologyChange.isAddOntologyAnnotation()) {
                ontologyIds.add(ontologyChange.getOntologyId());
            }
            else if(ontologyChange.isRemoveAxiom() || ontologyChange.isRemoveOntologyAnnotation()) {
                ontologyIds.remove(ontologyChange.getOntologyId());
            }
        }
        cache = ImmutableList.copyOf(ontologyIds.elementSet());
        initialized = true;
    }
}
