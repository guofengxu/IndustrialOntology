package org.industrial.ontology.kernel.frame.translator;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Stream;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.EquivalentClassesAxiom2PropertyValuesTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 * <p>
 * Public, unlike the legacy class, so that {@code ProjectContextFactory} can wire it without Dagger.
 */
public class EquivalentClassesAxiom2PropertyValuesTranslator {

    public EquivalentClassesAxiom2PropertyValuesTranslator(@Nonnull ClassExpression2PropertyValuesTranslator classExpression2PropertyValuesTranslator) {
        this.classExpression2PropertyValuesTranslator = classExpression2PropertyValuesTranslator;
    }

    @Nonnull
    private final ClassExpression2PropertyValuesTranslator classExpression2PropertyValuesTranslator;

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLEquivalentClassesAxiom axiom,
                                             @Nonnull OWLEntity subject,
                                             @Nonnull State initialState) {
        if(!subject.isOWLClass()) {
            return ImmutableSet.of();
        }
        var classExpressions = axiom.getClassExpressions();
        if(!classExpressions.contains(subject.asOWLClass())) {
            return ImmutableSet.of();
        }
        return classExpressions.stream()
                               .filter(ce -> !ce.equals(subject))
                               .flatMap(this::toDerivedPlainPropertyValues)
                               .collect(toImmutableSet());
    }

    private Stream<? extends PlainPropertyValue> toDerivedPlainPropertyValues(OWLClassExpression ce) {
        return classExpression2PropertyValuesTranslator.translate(State.DERIVED, ce)
                                                       .stream();
    }
}
