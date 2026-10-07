package org.industrial.ontology.kernel.api.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.Matcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Jun 2018
 */
public interface Matcher<T>  {

    boolean matches(@Nonnull T value);

    static <T> Matcher<T> matchesAny() {
        return o -> true;
    }
}
