package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.AnnotationAssertionAxiom2PropertyValuesTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public class AnnotationAssertionAxiom2PropertyValuesTranslator {

    @Nonnull
    private final Annotation2PropertyValueTranslator annotation2PropertyValueTranslator;

    public AnnotationAssertionAxiom2PropertyValuesTranslator(@Nonnull Annotation2PropertyValueTranslator annotation2PropertyValueTranslator) {
        this.annotation2PropertyValueTranslator = annotation2PropertyValueTranslator;
    }

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLAnnotationAssertionAxiom axiom,
                                              @Nonnull OWLEntity subject,
                                              @Nonnull State initialState) {
        if(!axiom.getSubject().equals(subject.getIRI())) {
            return Collections.emptySet();
        }
        return annotation2PropertyValueTranslator.translate(axiom.getAnnotation(), initialState);
    }
}
