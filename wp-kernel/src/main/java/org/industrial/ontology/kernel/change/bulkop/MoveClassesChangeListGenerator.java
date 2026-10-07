package org.industrial.ontology.kernel.change.bulkop;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;

import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;

import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.bulkop.MoveClassesChangeListGenerator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 25 Sep 2018
 */
public class MoveClassesChangeListGenerator implements ChangeListGenerator<Boolean> {

    @Nonnull
    private final ImmutableSet<OWLClass> childClasses;

    @Nonnull
    private final OWLClass targetParent;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologies;

    @Nonnull
    private final SubClassOfAxiomsBySubClassIndex subClassAxiomIndex;

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final String commitMessage;

    public MoveClassesChangeListGenerator(@Nonnull ImmutableSet<OWLClass> childClasses,
                                          @Nonnull OWLClass targetParent,
                                          @Nonnull String commitMessage,
                                          @Nonnull ProjectOntologiesIndex projectOntologies,
                                          @Nonnull SubClassOfAxiomsBySubClassIndex subClassAxiomIndex,
                                          @Nonnull OWLDataFactory dataFactory) {
        this.childClasses = checkNotNull(childClasses);
        this.targetParent = checkNotNull(targetParent);
        this.projectOntologies = checkNotNull(projectOntologies);
        this.subClassAxiomIndex = checkNotNull(subClassAxiomIndex);
        this.dataFactory = checkNotNull(dataFactory);
        this.commitMessage = checkNotNull(commitMessage);
    }

    @Override
    public OntologyChangeList<Boolean> generateChanges(ChangeGenerationContext context) {
        var changeList = new OntologyChangeList.Builder<Boolean>();
        projectOntologies.getOntologyIds().forEach(ontId -> {
            childClasses.forEach(childClass -> {
                subClassAxiomIndex
                        .getSubClassOfAxiomsForSubClass(childClass, ontId)
                        .filter(ax -> ax.getSuperClass().isNamed())
                        .filter(ax -> !ax.getSuperClass().equals(targetParent))
                        .forEach(ax -> processAxiom(ax, childClass, ontId, changeList));
            });
        });
        return changeList.build(true);
    }

    private void processAxiom(OWLSubClassOfAxiom ax,
                              OWLClass childCls,
                              OWLOntologyID ontId,
                              OntologyChangeList.Builder<Boolean> changeList) {
        var removeAxiom = RemoveAxiomChange.of(ontId, ax);
        changeList.add(removeAxiom);
        var replacementAx = dataFactory.getOWLSubClassOfAxiom(childCls, targetParent, ax.getAnnotations());
        var addAxiom = AddAxiomChange.of(ontId, replacementAx);
        changeList.add(addAxiom);
    }

    @Override
    public Boolean getRenamedResult(Boolean result,
                                    RenameMap renameMap) {
        return result;
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<Boolean> result) {
        return commitMessage;
    }
}
