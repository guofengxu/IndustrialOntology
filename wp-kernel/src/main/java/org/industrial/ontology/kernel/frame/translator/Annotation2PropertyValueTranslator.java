package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLAnnotation;
import javax.annotation.Nonnull;
import java.util.Collections;

import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.Annotation2PropertyValueTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-14
 */
public class Annotation2PropertyValueTranslator {

    public Annotation2PropertyValueTranslator() {
    }

    @Nonnull
    public Set<PlainPropertyValue> translate(@Nonnull OWLAnnotation annotation,
                                             @Nonnull State state) {
        return Collections.singleton(PlainPropertyAnnotationValue.get(annotation.getProperty(),
                                                                      annotation.getValue(),
                                                                      state));

    }
}
