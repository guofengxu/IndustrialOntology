package org.industrial.ontology.kernel.match;



import org.industrial.ontology.domain.match.StringContainsCriteria;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringContainsMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-23
 */
public class StringContainsMatcher implements Matcher<String> {

    @Nonnull
    private final StringContainsCriteria criteria;

    public StringContainsMatcher(@Nonnull StringContainsCriteria criteria) {
        this.criteria = checkNotNull(criteria);
    }

    @Override
    public boolean matches(@Nonnull String value) {
        if(criteria.isIgnoreCase()) {
            return StringUtils.containsIgnoreCase(value, criteria.getValue());
        }
        else {
            return value.contains(criteria.getValue());
        }
    }
}
