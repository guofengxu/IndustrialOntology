package org.industrial.ontology.kernel.match;



import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringHasUntrimmedSpaceMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 13 Jun 2018
 */
public class StringHasUntrimmedSpaceMatcher implements Matcher<String> {

    @Override
    public boolean matches(@Nonnull String value) {
        return value.endsWith(" ") || value.startsWith(" ");
    }
}
