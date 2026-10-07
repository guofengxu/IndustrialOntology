package org.industrial.ontology.kernel.lucene;

import org.industrial.ontology.kernel.api.match.Matcher;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntitySearchFilterMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-21
 */
public record EntitySearchFilterMatcher(@Nonnull EntitySearchFilter filter, @Nonnull Matcher<OWLEntity> matcher) {

    public EntitySearchFilterMatcher {
        Objects.requireNonNull(filter, "Null filter");
        Objects.requireNonNull(matcher, "Null matcher");
    }

    @Nonnull
    public static EntitySearchFilterMatcher get(@Nonnull EntitySearchFilter searchFilter, @Nonnull Matcher<OWLEntity> matcher) {
        return new EntitySearchFilterMatcher(searchFilter, matcher);
    }

    @Nonnull
    public EntitySearchFilter getFilter() {
        return filter;
    }

    @Nonnull
    public Matcher<OWLEntity> getMatcher() {
        return matcher;
    }
}
