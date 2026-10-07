package org.industrial.ontology.domain.match;



import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RegexMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public interface RegexMatchCriteria extends LexicalValueCriteria {

    String PATTERN = "pattern";

    String IGNORE_CASE = "ignoreCase";

    @JsonProperty(PATTERN)
    @Nonnull
    String getPattern();

    @JsonProperty(IGNORE_CASE)
    boolean isIgnoreCase();
}
