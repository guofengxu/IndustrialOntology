package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.kernel.frame.Mode;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.PropertyValue2AxiomTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-03
 */
public class PropertyValue2AxiomTranslator {

    public PropertyValue2AxiomTranslator() {
    }

    @Nonnull
    public Set<OWLAxiom> getAxioms(OWLEntity subject,
                                   PlainPropertyValue propertyValue,
                                   Mode mode) {
        if (propertyValue.getState() == State.DERIVED) {
            return Collections.emptySet();
        }
        PropertyValueTranslator translator = new PropertyValueTranslator(subject, mode);
        return propertyValue.accept(translator);
    }
}
