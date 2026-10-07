package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.OntologyAnnotationsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;

import org.industrial.ontology.kernel.api.change.ChangeListMinimiser;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitorEx;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.RootIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-10
 */
public class RootIndex implements org.industrial.ontology.kernel.api.index.RootIndex {

    @Nonnull
    private final OntologyAxiomsIndex ontologyAxiomsIndex;

    @Nonnull
    private final OntologyAnnotationsIndex ontologyAnnotationsIndex;

    @Nonnull
    private final ChangeFilter changeFilter = new ChangeFilter();

    public RootIndex(@Nonnull OntologyAxiomsIndex ontologyAxiomsIndex,
                         @Nonnull OntologyAnnotationsIndex ontologyAnnotationsIndex) {
        this.ontologyAxiomsIndex = ontologyAxiomsIndex;
        this.ontologyAnnotationsIndex = ontologyAnnotationsIndex;
    }

    @Nonnull
    @Override
    public List<OntologyChange> getEffectiveChanges(@Nonnull List<OntologyChange> changes) {
        var minimizedChanges = getMinimizedChanges(changes);
        return minimizedChanges.stream()
                               .filter(this::isEffectiveChange)
                               .collect(toImmutableList());
    }

    private List<OntologyChange> getMinimizedChanges(@Nonnull List<OntologyChange> changes) {
        var changeListMinimizer = new ChangeListMinimiser();
        return changeListMinimizer.getMinimisedChanges(changes);
    }

    private boolean isEffectiveChange(OntologyChange chg) {
        return chg.accept(changeFilter)
                          .equals(Boolean.TRUE);
    }

    private class ChangeFilter implements OntologyChangeVisitorEx<Boolean> {

        @Override
        public Boolean visit(@Nonnull AddAxiomChange addAxiomChange) {
            var axiom = addAxiomChange.getAxiom();
            var ontologyId = addAxiomChange.getOntologyId();
            if(!ontologyAxiomsIndex.containsAxiom(axiom, ontologyId)) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }

        @Override
        public Boolean visit(@Nonnull RemoveAxiomChange removeAxiomChange) {
            var axiom = removeAxiomChange.getAxiom();
            var ontologyId = removeAxiomChange.getOntologyId();
            if(ontologyAxiomsIndex.containsAxiom(axiom, ontologyId)) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }

        @Override
        public Boolean visit(@Nonnull AddOntologyAnnotationChange addOntologyAnnotationChange) {
            var annotation = addOntologyAnnotationChange.getAnnotation();
            var ontologyId = addOntologyAnnotationChange.getOntologyId();
            if(!ontologyAnnotationsIndex.containsAnnotation(annotation, ontologyId)) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }

        @Override
        public Boolean visit(@Nonnull RemoveOntologyAnnotationChange removeOntologyAnnotationChange) {
            var annotation = removeOntologyAnnotationChange.getAnnotation();
            var ontologyId = removeOntologyAnnotationChange.getOntologyId();
            if(ontologyAnnotationsIndex.containsAnnotation(annotation, ontologyId)) {
                return Boolean.TRUE;
            }
            else {
                return Boolean.FALSE;
            }
        }
    }
}
