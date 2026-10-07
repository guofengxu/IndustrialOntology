package org.industrial.ontology.kernel.change.matcher;



import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.SetDataPropertyFunctional;
import org.industrial.ontology.kernel.change.description.UnsetDataPropertyFunctional;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.FunctionalDataPropertyAxiomChangeMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class FunctionalDataPropertyAxiomChangeMatcher extends AbstractAxiomMatcher<OWLFunctionalDataPropertyAxiom> {

    public FunctionalDataPropertyAxiomChangeMatcher() {
        super(new TypeToken<OWLFunctionalDataPropertyAxiom>(){});
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLFunctionalDataPropertyAxiom axiom,
                                                                      List<OntologyChange> changes) {
        return Optional.of(ChangeSummary.get(SetDataPropertyFunctional.get(axiom.getProperty().asOWLDataProperty())));
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLFunctionalDataPropertyAxiom axiom) {
        return Optional.of(ChangeSummary.get(UnsetDataPropertyFunctional.get(axiom.getProperty().asOWLDataProperty())));
    }
}
