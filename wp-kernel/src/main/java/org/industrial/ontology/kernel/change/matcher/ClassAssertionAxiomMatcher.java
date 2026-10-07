package org.industrial.ontology.kernel.change.matcher;



import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.AddedIndividualType;
import org.industrial.ontology.kernel.change.description.AddedRelationship;
import org.industrial.ontology.kernel.change.description.RemovedIndividualType;
import org.industrial.ontology.kernel.change.description.RemovedRelationship;
import org.semanticweb.owlapi.change.AddAxiomData;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.ClassAssertionAxiomMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class ClassAssertionAxiomMatcher extends AbstractAxiomMatcher<OWLClassAssertionAxiom> {

    public ClassAssertionAxiomMatcher() {
        super(new TypeToken<OWLClassAssertionAxiom>(){});
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLClassAssertionAxiom axiom,
                                                                      List<OntologyChange> changes) {
        var possibleEntityCreation = changes.stream()
                .filter(data -> data instanceof AddAxiomData)
                .map(data -> ((AddAxiomData) data).getAxiom())
                .filter(ax -> ax instanceof OWLDeclarationAxiom)
                .map(ax -> ((OWLDeclarationAxiom) ax).getEntity())
                .anyMatch(entity -> entity.equals(axiom.getIndividual()));
        if(possibleEntityCreation) {
            return Optional.empty();
        }
        PropertyFiller propertyFiller = new PropertyFiller(axiom.getIndividual(),
                                                           axiom.getClassExpression());
        Optional<OWLProperty> property = propertyFiller.getProperty();
        Optional<OWLObject> filler = propertyFiller.getFiller();
        if(property.isPresent() && filler.isPresent()) {
            return Optional.of(ChangeSummary.get(AddedRelationship.get(axiom.getIndividual(),
                                                                       property.get(),
                                                                       filler.get())));
        }
        else {
            return Optional.of(ChangeSummary.get(AddedIndividualType.get(axiom.getIndividual(),
                                                                         axiom.getClassExpression().asOWLClass())));
        }
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLClassAssertionAxiom axiom) {
        PropertyFiller propertyFiller = new PropertyFiller(axiom.getIndividual(),
                                                           axiom.getClassExpression());
        Optional<OWLProperty> property = propertyFiller.getProperty();
        Optional<OWLObject> filler = propertyFiller.getFiller();
        if(property.isPresent() && filler.isPresent()) {
            return Optional.of(ChangeSummary.get(RemovedRelationship.get(axiom.getIndividual(),
                                                                         property.get(),
                                                                         filler.get())));
        }
        else {
            return Optional.of(ChangeSummary.get(RemovedIndividualType.get(axiom.getIndividual(),
                                                                           axiom.getClassExpression().asOWLClass())));
        }
    }

    @Override
    protected boolean allowSignatureDeclarations() {
        return true;
    }
}
