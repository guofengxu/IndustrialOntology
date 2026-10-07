package org.industrial.ontology.kernel.match;



import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.industrial.ontology.domain.match.AnnotationPresence;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityIsDeprecatedMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
public class EntityIsDeprecatedMatcher implements Matcher<OWLEntity> {

    private final Matcher<OWLEntity> delegate;

    public EntityIsDeprecatedMatcher(@Nonnull AnnotationAssertionAxiomsIndex axioms) {
        final AnnotationMatcher deprecatedMatcher = new AnnotationMatcher(
                OWLAnnotationProperty::isDeprecated,
                LiteralAnnotationValueMatcher.forIsXsdBooleanTrue()
        );
        delegate = new EntityAnnotationMatcher(axioms,
                                               deprecatedMatcher,
                                               AnnotationPresence.AT_LEAST_ONE);
    }

    @Override
    public boolean matches(@Nonnull OWLEntity value) {
        return delegate.matches(value);
    }
}
