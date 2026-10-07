package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.ClassAssertionAxiom2PropertyValuesTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public class ClassAssertionAxiom2PropertyValuesTranslator {

    @Nonnull
    private final ClassExpression2PropertyValuesTranslator classExpression2PropertyValuesTranslator;

    public ClassAssertionAxiom2PropertyValuesTranslator(@Nonnull ClassExpression2PropertyValuesTranslator classExpression2PropertyValuesTranslator) {
        this.classExpression2PropertyValuesTranslator = checkNotNull(classExpression2PropertyValuesTranslator);
    }

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLClassAssertionAxiom axiom,
                                              @Nonnull OWLEntity subject,
                                              @Nonnull State initialState) {
        if(!axiom.getIndividual().equals(subject)) {
            return Collections.emptySet();
        }
        var classExpression = axiom.getClassExpression();
        return classExpression2PropertyValuesTranslator.translate(initialState, classExpression);
    }
}
