package org.industrial.ontology.kernel.change.matcher;



import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.AddedRelationship;
import org.industrial.ontology.kernel.change.description.RemovedRelationship;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.PropertyAssertionAxiomMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 13 Mar 2017
 */
public class PropertyAssertionAxiomMatcher extends AbstractAxiomMatcher<OWLPropertyAssertionAxiom<?,?>> {

    public PropertyAssertionAxiomMatcher() {
        super(new TypeToken<OWLPropertyAssertionAxiom<?,?>>() {});
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLPropertyAssertionAxiom<?, ?> axiom,
                                                                      List<OntologyChange> changes) {
        return Optional.of(ChangeSummary.get(AddedRelationship.get(axiom.getSubject(),
                                                                   (OWLProperty) axiom.getProperty(),
                                                                   axiom.getObject())));
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLPropertyAssertionAxiom<?,?> axiom) {
        return Optional.of(ChangeSummary.get(RemovedRelationship.get(axiom.getSubject(),
                                                                     (OWLProperty) axiom.getProperty(),
                                                                     axiom.getObject())));
    }

    @Override
    protected boolean allowSignatureDeclarations() {
        return true;
    }
}
