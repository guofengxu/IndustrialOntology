package org.industrial.ontology.kernel.match;



import org.industrial.ontology.domain.match.StringEqualsCriteria;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringEqualsMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-23
 */
public class StringEqualsMatcher implements Matcher<String> {

    @Nonnull
    private final StringEqualsCriteria criteria;

    public StringEqualsMatcher(@Nonnull StringEqualsCriteria criteria) {
        this.criteria = checkNotNull(criteria);
    }

    @Override
    public boolean matches(@Nonnull String value) {
        if(criteria.isIgnoreCase()) {
            return StringUtils.equalsIgnoreCase(criteria.getValue(), value);
        }
        else {
            return criteria.getValue().equals(value);
        }
    }
}
