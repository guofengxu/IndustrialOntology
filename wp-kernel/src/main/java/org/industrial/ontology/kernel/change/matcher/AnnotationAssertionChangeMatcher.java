package org.industrial.ontology.kernel.change.matcher;



import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.description.AddedAnnotation;
import org.industrial.ontology.kernel.change.description.DeprecatedEntities;
import org.industrial.ontology.kernel.change.description.RemovedAnnotation;
import org.industrial.ontology.kernel.change.description.UndeprecatedEntities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;

import java.util.List;
import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.AnnotationAssertionChangeMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class AnnotationAssertionChangeMatcher extends AbstractAxiomMatcher<OWLAnnotationAssertionAxiom> {

    public AnnotationAssertionChangeMatcher() {
        super(new TypeToken<OWLAnnotationAssertionAxiom>(){});
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForAddAxiomChange(OWLAnnotationAssertionAxiom axiom,
                                                                      List<OntologyChange> changes) {
        if(!axiom.getSubject().isIRI()) {
            return Optional.empty();
        }
        if(axiom.getProperty().isDeprecated()) {
            return Optional.of(ChangeSummary.get(DeprecatedEntities.get(ImmutableSet.of((IRI) axiom.getSubject()))));
        }
        return Optional.of(ChangeSummary.get(AddedAnnotation.get((IRI) axiom.getSubject(),
                                                                 axiom.getProperty(),
                                                                 axiom.getValue())));
    }

    @Override
    protected Optional<ChangeSummary> getDescriptionForRemoveAxiomChange(OWLAnnotationAssertionAxiom axiom) {
        if(!axiom.getSubject().isIRI()) {
            return Optional.empty();
        }
        var subject = (IRI) axiom.getSubject();
        if(axiom.getProperty().isDeprecated()) {
            return Optional.of(ChangeSummary.get(UndeprecatedEntities.get(ImmutableSet.of(subject))));
        }
        return Optional.of(ChangeSummary.get(RemovedAnnotation.get(subject,
                                                                   axiom.getProperty(),
                                                                   axiom.getValue())));
    }
}
