package org.industrial.ontology.kernel.match;



import com.google.common.primitives.Doubles;
import org.industrial.ontology.domain.match.NumericPredicate;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.NumericValueMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
public class NumericValueMatcher implements Matcher<String> {

    @Nonnull
    private final NumericPredicate predicate;

    private final double value;

    public NumericValueMatcher(@Nonnull NumericPredicate predicate, double value) {
        this.predicate = checkNotNull(predicate);
        this.value = value;
    }

    @Override
    public boolean matches(@Nonnull String s) {
        if(s.isEmpty()) {
            return false;
        }
        // We use Doubles.tryParse because failures are expected
        // and Double.parseDouble uses exceptions to indicate failure,
        // which are expensive.
        Double d = Doubles.tryParse(s);
        return d != null && predicate.eval(d, value);
    }
}
