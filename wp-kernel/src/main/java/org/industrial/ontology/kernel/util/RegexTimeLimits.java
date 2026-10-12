package org.industrial.ontology.kernel.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Time limits for matching user-supplied regular expressions (match criteria, search filters, bulk edits), which can
 * take exponential time on some values and cannot be interrupted. The kernel's regex matchers create their
 * {@link Matcher}s through {@link #matcher}; inside a limit scope opened by {@link #withBudget} or
 * {@link #withLimitPerMatch}, matching that runs past its deadline throws {@link Exceeded}.
 * <p>
 * Outside a scope nothing is limited, as in the legacy server. That is deliberate: {@code ChangeManager} evaluates
 * search filters while it writes the Lucene documents of a change, after the indexes were updated and before the
 * revision is logged, and an exception there would leave the project inconsistent. Callers open a scope only around
 * work that may be abandoned, such as reads and change generation.
 */
public final class RegexTimeLimits {

    /** How many characters are read between two clock checks. */
    private static final int CHECK_INTERVAL = 1024;

    private static final ThreadLocal<Limits> LIMITS = new ThreadLocal<>();

    private RegexTimeLimits() {
    }

    /**
     * Runs the work with a budget for all the matching that it does.
     */
    public static <T> T withBudget(@Nonnull Duration budget, @Nonnull Supplier<T> work) {
        return within(new Limits(System.nanoTime() + budget.toNanos(), Long.MAX_VALUE), work);
    }

    /**
     * Runs the work with a limit for each match.
     */
    public static <T> T withLimitPerMatch(@Nonnull Duration perMatch, @Nonnull Supplier<T> work) {
        return within(new Limits(Long.MAX_VALUE, perMatch.toNanos()), work);
    }

    /**
     * A matcher of the pattern for the input, limited by the scope of the current thread if there is one.
     */
    @Nonnull
    public static Matcher matcher(@Nonnull Pattern pattern, @Nonnull CharSequence input) {
        var limits = LIMITS.get();
        if (limits == null) {
            return pattern.matcher(input);
        }
        return pattern.matcher(new LimitedCharSequence(input, limits.deadline(System.nanoTime())));
    }

    private static <T> T within(Limits limits, Supplier<T> work) {
        checkNotNull(work);
        var outer = LIMITS.get();
        LIMITS.set(outer == null ? limits : outer.narrowedBy(limits));
        try {
            return work.get();
        } finally {
            if (outer == null) {
                LIMITS.remove();
            } else {
                LIMITS.set(outer);
            }
        }
    }

    /**
     * Matching a regular expression took longer than its scope allows.
     */
    public static final class Exceeded extends RuntimeException {

        private Exceeded() {
            super("Matching a regular expression took too long");
        }
    }

    /**
     * @param budgetDeadline the nano time by which all matching must end
     * @param perMatchNanos  how long each match may take
     */
    private record Limits(long budgetDeadline, long perMatchNanos) {

        long deadline(long now) {
            var perMatch = perMatchNanos == Long.MAX_VALUE ? Long.MAX_VALUE : now + perMatchNanos;
            return Math.min(budgetDeadline, perMatch);
        }

        Limits narrowedBy(@Nullable Limits inner) {
            return inner == null ? this : new Limits(Math.min(budgetDeadline, inner.budgetDeadline),
                                                     Math.min(perMatchNanos, inner.perMatchNanos));
        }
    }

    /**
     * Reads the input and checks the clock every {@value #CHECK_INTERVAL} characters, which the regex engine reads
     * one at a time.
     */
    private static final class LimitedCharSequence implements CharSequence {

        private final CharSequence text;

        private final long deadline;

        private int reads;

        private LimitedCharSequence(CharSequence text, long deadline) {
            this.text = text;
            this.deadline = deadline;
        }

        @Override
        public char charAt(int index) {
            if (++reads % CHECK_INTERVAL == 0 && System.nanoTime() > deadline) {
                throw new Exceeded();
            }
            return text.charAt(index);
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new LimitedCharSequence(text.subSequence(start, end), deadline);
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }
}
