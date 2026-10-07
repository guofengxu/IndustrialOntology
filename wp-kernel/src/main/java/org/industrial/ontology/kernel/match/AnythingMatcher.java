package org.industrial.ontology.kernel.match;



import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.AnythingMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
public class AnythingMatcher<T> implements Matcher<T> {

    public static <S> AnythingMatcher<S> get() {
        return new AnythingMatcher<>();
    }

    @Override
    public boolean matches(@Nonnull T value) {
        return true;
    }
}
