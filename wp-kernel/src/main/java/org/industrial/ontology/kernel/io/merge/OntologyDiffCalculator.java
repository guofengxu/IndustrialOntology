package org.industrial.ontology.kernel.io.merge;

import org.industrial.ontology.domain.merge.Diff;
import org.industrial.ontology.domain.merge.OntologyDiff;
import org.industrial.ontology.kernel.project.Ontology;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.OntologyDiffCalculator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/01/15
 */
public class OntologyDiffCalculator {


    private AnnotationDiffCalculator annotationDiffCalculator;

    private AxiomDiffCalculator axiomDiffCalculator;

    public OntologyDiffCalculator(AnnotationDiffCalculator annotationDiffCalculator, AxiomDiffCalculator axiomDiffCalculator) {
        this.annotationDiffCalculator = annotationDiffCalculator;
        this.axiomDiffCalculator = axiomDiffCalculator;
    }

    public OntologyDiff computeDiff(Ontology from, Ontology to) {
        Diff<OWLAxiom> axiomDiff = axiomDiffCalculator.computeDiff(from, to);
        Diff<OWLAnnotation> annotationDiff = annotationDiffCalculator.computeDiff(from, to);
        return new OntologyDiff(from.getOntologyId(), to.getOntologyId(), annotationDiff, axiomDiff);
    }
}
