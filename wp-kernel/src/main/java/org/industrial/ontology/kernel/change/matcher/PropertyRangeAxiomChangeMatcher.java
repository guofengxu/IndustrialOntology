package org.industrial.ontology.kernel.change.matcher;



import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.AddedPropertyRange;
import org.industrial.ontology.kernel.change.description.RemovedPropertyRange;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyRangeAxiom;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.PropertyRangeAxiomChangeMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class PropertyRangeAxiomChangeMatcher extends AbstractAxiomMatcher<OWLPropertyRangeAxiom<?,?>> {

    public PropertyRangeAxiomChangeMatcher() {
        super(new TypeToken<OWLPropertyRangeAxiom<?,?>>(){});
    }

    @Override
    protected boolean allowSignatureDeclarations() {
        return true;
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLPropertyRangeAxiom<?, ?> axiom,
                                                                      List<OntologyChange> changes) {
        return Optional.of(ChangeSummary.get(AddedPropertyRange.get((OWLProperty) axiom.getProperty(),
                                                                    axiom.getRange())));
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLPropertyRangeAxiom<?, ?> axiom) {
        return Optional.of(ChangeSummary.get(RemovedPropertyRange.get((OWLProperty) axiom.getProperty(),
                                                                      axiom.getRange())));
    }
}
