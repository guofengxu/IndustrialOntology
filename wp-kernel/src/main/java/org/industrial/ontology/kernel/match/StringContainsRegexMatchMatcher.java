package org.industrial.ontology.kernel.match;



import javax.annotation.Nonnull;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringContainsRegexMatchMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
public class StringContainsRegexMatchMatcher implements Matcher<String> {

    @Nonnull
    private final Pattern pattern;

    public StringContainsRegexMatchMatcher(@Nonnull Pattern pattern) {
        this.pattern = checkNotNull(pattern);
    }

    @Override
    public boolean matches(@Nonnull String value) {
        return pattern.matcher(value).find();
    }
}
