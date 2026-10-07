package org.industrial.ontology.kernel.change.matcher;



import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.AddedPropertyDomain;
import org.industrial.ontology.kernel.change.description.RemovedPropertyDomain;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyDomainAxiom;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.PropertyDomainAxiomChangeMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class PropertyDomainAxiomChangeMatcher extends AbstractAxiomMatcher<OWLPropertyDomainAxiom<?>> {

    public PropertyDomainAxiomChangeMatcher() {
        super(new TypeToken<OWLPropertyDomainAxiom<?>>(){});
    }

    @Override
    protected boolean allowSignatureDeclarations() {
        return true;
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLPropertyDomainAxiom<?> axiom,
                                                                      List<OntologyChange> changes) {
        return Optional.of(ChangeSummary.get(AddedPropertyDomain.get((OWLProperty) axiom.getProperty(), axiom.getDomain())));
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLPropertyDomainAxiom<?> axiom) {
        return Optional.of(ChangeSummary.get(RemovedPropertyDomain.get((OWLProperty) axiom.getProperty(), axiom.getDomain())));
    }
}
