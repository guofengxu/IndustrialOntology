package org.industrial.ontology.kernel.diff;



import org.industrial.ontology.domain.merge.Diff;
import org.industrial.ontology.domain.merge.OntologyDiff;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;

import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.diff.OntologyDiff2OntologyChanges}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/03/15
 */
public class OntologyDiff2OntologyChanges {

    public OntologyDiff2OntologyChanges() {
    }

    @Nonnull
    public List<OntologyChange> getOntologyChangesFromDiff(@Nonnull OntologyDiff diff) {
        checkNotNull(diff);
        var changeList = new ArrayList<OntologyChange>();
        var ont = diff.getFromOntologyId();
        Diff<OWLAnnotation> annotationDiff = diff.getAnnotationDiff();
        for (OWLAnnotation anno : annotationDiff.getAdded()) {
            changeList.add(AddOntologyAnnotationChange.of(ont, anno));
        }
        for (OWLAnnotation anno : annotationDiff.getRemoved()) {
            changeList.add(RemoveOntologyAnnotationChange.of(ont, anno));
        }
        Diff<OWLAxiom> axiomDiff = diff.getAxiomDiff();
        for (OWLAxiom axiom : axiomDiff.getRemoved()) {
            changeList.add(RemoveAxiomChange.of(ont, axiom));
        }
        for (OWLAxiom axiom : axiomDiff.getAdded()) {
            changeList.add(AddAxiomChange.of(ont, axiom));
        }
        return changeList;
    }
}
