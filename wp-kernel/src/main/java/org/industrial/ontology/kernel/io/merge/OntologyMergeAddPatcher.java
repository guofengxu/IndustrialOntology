package org.industrial.ontology.kernel.io.merge;

import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.change.HasApplyChanges;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge_add.OntologyMergeAddPatcher}.
 * <p>
 * The methods were package-private in the legacy code, where only the (not ported) merge-add action handlers of the
 * same package called them; they are public here so that the {@code wp-app} services replacing those handlers in P4
 * can call them. {@link #applyChanges} takes the {@link UserId} that it used to read from the dispatch
 * {@code ExecutionContext}.
 */
public class OntologyMergeAddPatcher {

    @Nonnull
    private final HasApplyChanges changeManager;

    public OntologyMergeAddPatcher(@Nonnull HasApplyChanges changeManager) {
        this.changeManager = changeManager;
    }

    public List<OntologyChange> addAxiomsAndAnnotations(Set<OWLAxiom> axioms, Set<OWLAnnotation> annotations, OWLOntologyID ontologyID){
        var changeList = new ArrayList<OntologyChange>();

        for (OWLAxiom axiom: axioms) {
            changeList.add(AddAxiomChange.of(ontologyID,axiom));
        }

        for (OWLAnnotation annotation: annotations){
            changeList.add(AddOntologyAnnotationChange.of(ontologyID, annotation));
        }

        return changeList;
    }

    public void applyChanges(final List<OntologyChange> changes,
                             UserId userId) {
        changeManager.applyChanges(userId, new ChangeListGenerator<Boolean>() {
            @Override
            public OntologyChangeList<Boolean> generateChanges(ChangeGenerationContext context) {
                OntologyChangeList.Builder<Boolean> builder = OntologyChangeList.builder();
                builder.addAll(changes);
                return builder.build(!changes.isEmpty());
            }

            @Override
            public Boolean getRenamedResult(Boolean result, RenameMap renameMap) {
                return true;
            }

            @Nonnull
            @Override
            public String getMessage(ChangeApplicationResult<Boolean> result) {
                return "Merge ontologies into new ontology";
            }
        });

    }
}
