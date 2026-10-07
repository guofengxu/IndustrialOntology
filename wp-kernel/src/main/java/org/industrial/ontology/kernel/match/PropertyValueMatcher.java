package org.industrial.ontology.kernel.match;



import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLProperty;

import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.PropertyValueMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
public class PropertyValueMatcher implements Matcher<PlainPropertyValue> {

    @Nonnull
    private final Matcher<OWLProperty> propertyMatcher;

    @Nonnull
    private final Matcher<OWLPrimitive> valueMatcher;

    public PropertyValueMatcher(@Nonnull Matcher<OWLProperty> propertyMatcher,
                                @Nonnull Matcher<OWLPrimitive> valueMatcher) {
        this.propertyMatcher = propertyMatcher;
        this.valueMatcher = valueMatcher;
    }

    @Override
    public boolean matches(@Nonnull PlainPropertyValue value) {
        return propertyMatcher.matches(value.getProperty())
                && valueMatcher.matches(value.getValue());
    }
}
