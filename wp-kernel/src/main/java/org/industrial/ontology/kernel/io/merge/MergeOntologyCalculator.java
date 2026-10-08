package org.industrial.ontology.kernel.io.merge;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.project.Ontology;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge_add.MergeOntologyCalculator}.
 * <p>
 * The methods were package-private in the legacy code, where only the (not ported) merge-add action handlers of the
 * same package called them; they are public here so that the {@code wp-app} services replacing those handlers in P4
 * can call them.
 */
public class MergeOntologyCalculator {

    public MergeOntologyCalculator() {
    }

    public ImmutableSet<OWLAxiom> getMergeAxioms(Collection<Ontology> projectOntologies, Collection<Ontology> uploadedOntologies, List<OWLOntologyID> ontologyList){
        ArrayList<OWLOntologyID> list = (ArrayList<OWLOntologyID>) ontologyList;
        var axioms = ImmutableSet.<OWLAxiom>builder();
        for (Ontology o: projectOntologies) {
            if(list.contains(o.getOntologyId())) {
                var projectAxioms = o.getAxioms();
                for (OWLAxiom x : projectAxioms) {
                    axioms.add(x);
                }
            }
        }
        for (Ontology o: uploadedOntologies){
            if (list.contains(o.getOntologyId())){
                var uploadedAxioms = o.getAxioms();
                for (OWLAxiom x: uploadedAxioms) {
                    axioms.add(x);
                }
            }
        }
        return axioms.build();
    }

    public ImmutableSet<OWLAnnotation> getMergeAnnotations(Collection<Ontology> projectOntologies, Collection<Ontology> uploadedOntologies, List<OWLOntologyID> ontologyList){
        ArrayList<OWLOntologyID> list = (ArrayList<OWLOntologyID>) ontologyList;
        var annotations = ImmutableSet.<OWLAnnotation>builder();
        for (Ontology o: projectOntologies) {
            if(list.contains(o.getOntologyId())) {
                var projectAnnotations = o.getAnnotations();
                for (OWLAnnotation x : projectAnnotations) {
                    annotations.add(x);
                }
            }
        }
        for (Ontology o: uploadedOntologies){
            if (list.contains(o.getOntologyId())){
                var uploadedAnnotations = o.getAnnotations();
                for (OWLAnnotation x: uploadedAnnotations) {
                    annotations.add(x);
                }
            }
        }
        return annotations.build();
    }

}
