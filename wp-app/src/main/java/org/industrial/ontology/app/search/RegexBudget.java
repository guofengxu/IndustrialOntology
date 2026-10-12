package org.industrial.ontology.app.search;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.kernel.util.RegexTimeLimits;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;

/**
 * Runs request work that matches user-supplied regular expressions (match criteria, bulk edits, search filters)
 * within a time budget, and reports the expressions' failures as the caller's errors: 400 {@value #REGEX_TIMEOUT} for
 * an expression that takes too long, 400 {@code INVALID_REQUEST} for one that does not compile. Without the budget, a
 * catastrophically backtracking expression would hold the project's read lock, or its change lock, indefinitely.
 */
public final class RegexBudget {

    public static final String REGEX_TIMEOUT = "REGEX_TIMEOUT";

    /** All the matching of one request. */
    public static final Duration REQUEST_BUDGET = Duration.ofSeconds(5);

    /** One match while the project's search index is rebuilt, which may take long for a large project. */
    public static final Duration REBUILD_LIMIT_PER_MATCH = Duration.ofSeconds(1);

    private RegexBudget() {
    }

    /**
     * Runs the work within {@link #REQUEST_BUDGET}.
     *
     * @throws WpException {@value #REGEX_TIMEOUT} or {@code INVALID_REQUEST}
     */
    public static <T> T limited(@Nonnull Supplier<T> work) {
        try {
            return RegexTimeLimits.withBudget(REQUEST_BUDGET, work);
        } catch (RegexTimeLimits.Exceeded e) {
            throw timeout();
        } catch (PatternSyntaxException e) {
            throw invalid(e);
        }
    }

    @Nonnull
    static WpException timeout() {
        return new WpException(REGEX_TIMEOUT, 400, "A regular expression took too long to match; simplify it");
    }

    @Nonnull
    static WpException invalid(@Nonnull PatternSyntaxException e) {
        return WpException.invalidRequest("Not a regular expression: " + e.getDescription());
    }
}
