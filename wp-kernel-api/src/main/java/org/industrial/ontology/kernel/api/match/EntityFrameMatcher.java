package org.industrial.ontology.kernel.api.match;



import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityFrameMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Jun 2018
 */
public interface EntityFrameMatcher extends Matcher<OWLEntity> {

    boolean matches(@Nonnull OWLEntity entity);
}
