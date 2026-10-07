package org.industrial.ontology.kernel.match;



import org.industrial.ontology.domain.match.StringStartsWithCriteria;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringStartsWithMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-19
 */
public class StringStartsWithMatcher implements Matcher<String> {

    @Nonnull
    private final StringStartsWithCriteria criteria;

    public StringStartsWithMatcher(@Nonnull StringStartsWithCriteria criteria) {
        this.criteria = checkNotNull(criteria);
    }

    @Override
    public boolean matches(@Nonnull String value) {
        if(criteria.isIgnoreCase()) {
            return StringUtils.startsWithIgnoreCase(value, criteria.getValue());
        }
        else {
            return value.startsWith(criteria.getValue());
        }
    }
}
