package org.industrial.ontology.kernel.frame.translator;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.frame.PlainPropertyIndividualValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

import javax.annotation.Nonnull;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.ObjectPropertyAssertionAxiom2PropertyValuesTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public class ObjectPropertyAssertionAxiom2PropertyValuesTranslator {

    public ObjectPropertyAssertionAxiom2PropertyValuesTranslator() {
    }

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLObjectPropertyAssertionAxiom axiom,
                                              @Nonnull OWLEntity subject,
                                              @Nonnull State initialState) {
        if(axiom.getProperty().isAnonymous()) {
            return ImmutableSet.of();
        }
        if(axiom.getObject().isAnonymous()) {
            return ImmutableSet.of();
        }
        if(!axiom.getSubject().equals(subject)) {
            return ImmutableSet.of();
        }
        var property = axiom.getProperty().asOWLObjectProperty();
        var object = axiom.getObject().asOWLNamedIndividual();
        return ImmutableSet.of(PlainPropertyIndividualValue.get(property,
                                                                object,
                                                                 initialState));
    }
}
