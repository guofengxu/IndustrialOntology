package org.industrial.ontology.kernel.frame.translator;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.frame.PlainPropertyLiteralValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.DataPropertyAssertionAxiom2PropertyValuesTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public class DataPropertyAssertionAxiom2PropertyValuesTranslator {

    public DataPropertyAssertionAxiom2PropertyValuesTranslator() {
    }

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLDataPropertyAssertionAxiom axiom,
                                             @Nonnull OWLEntity subject,
                                             @Nonnull State initialState) {
        if(!axiom.getSubject()
                 .equals(subject)) {
            return Collections.emptySet();
        }
        OWLDataProperty property = axiom.getProperty()
                                        .asOWLDataProperty();
        return ImmutableSet.of(PlainPropertyLiteralValue.get(property,
                                                             axiom.getObject(),
                                                             initialState));
    }
}
