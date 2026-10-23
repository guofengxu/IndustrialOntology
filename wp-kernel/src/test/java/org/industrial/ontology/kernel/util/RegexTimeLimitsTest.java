package org.industrial.ontology.kernel.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The time limits of user-supplied regular expressions (S7 security review). Without a limit, the slow pattern runs
 * for hours: the timeout fails such a test instead.
 */
@Timeout(value = 30, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
public class RegexTimeLimitsTest {

    /** Takes exponential time on the {@link #SLOW_INPUT}: JDK 17 does not optimise the nested groups away. */
    private static final Pattern SLOW = Pattern.compile("((q+)+)+$");

    private static final String SLOW_INPUT = "q".repeat(40) + "!";

    @Test
    public void shouldStopSlowMatchingWithinABudget() {
        var start = System.nanoTime();
        assertThrows(RegexTimeLimits.Exceeded.class,
                     () -> RegexTimeLimits.withBudget(Duration.ofMillis(200),
                                                      () -> RegexTimeLimits.matcher(SLOW, SLOW_INPUT).find()));
        assertThat(Duration.ofNanos(System.nanoTime() - start).toSeconds(), is(lessThan(5L)));
    }

    @Test
    public void shouldStopSlowMatchingWithALimitPerMatch() {
        assertThrows(RegexTimeLimits.Exceeded.class,
                     () -> RegexTimeLimits.withLimitPerMatch(Duration.ofMillis(200),
                                                             () -> RegexTimeLimits.matcher(SLOW, SLOW_INPUT).find()));
    }

    @Test
    public void shouldMatchNormallyInsideAScope() {
        var found = RegexTimeLimits.withBudget(Duration.ofSeconds(5),
                                               () -> RegexTimeLimits.matcher(Pattern.compile("mozz"),
                                                                             "MozzarellaTopping mozzarella").find());
        assertThat(found, is(true));
    }

    @Test
    public void shouldNotLimitMatchingOutsideAScope() {
        var matcher = RegexTimeLimits.matcher(Pattern.compile("a+"), "aaa");
        assertThat(matcher.matches(), is(true));
        assertThat(matcher.pattern().pattern(), is("a+"));
    }

    @Test
    public void shouldNotLimitAMatcherCreatedOutsideAScope() {
        // Created outside any scope, the matcher reads its input directly, so an expired budget opened later does
        // not reach it
        var matcher = RegexTimeLimits.matcher(Pattern.compile("b+"), "b".repeat(5000));
        var found = RegexTimeLimits.withBudget(Duration.ZERO, matcher::matches);
        assertThat(found, is(true));
    }

    @Test
    public void shouldKeepTheNarrowerOuterBudgetInANestedScope() {
        assertThrows(RegexTimeLimits.Exceeded.class,
                     () -> RegexTimeLimits.withBudget(Duration.ofMillis(200),
                                                      () -> RegexTimeLimits.withLimitPerMatch(
                                                              Duration.ofHours(1),
                                                              () -> RegexTimeLimits.matcher(SLOW, SLOW_INPUT)
                                                                                   .find())));
    }

    @Test
    public void shouldRestoreTheOuterScopeAfterANestedScope() {
        assertThrows(RegexTimeLimits.Exceeded.class, () -> RegexTimeLimits.withBudget(Duration.ZERO, () -> {
            RegexTimeLimits.withBudget(Duration.ofHours(1), () -> true);
            // The expired outer budget applies again: reading 1024 characters checks the clock
            return RegexTimeLimits.matcher(Pattern.compile("c+"), "c".repeat(5000)).matches();
        }));
    }

    @Test
    public void shouldRemoveTheScopeAfterwards() {
        RegexTimeLimits.withBudget(Duration.ZERO, () -> true);
        var matcher = RegexTimeLimits.matcher(Pattern.compile("d+"), "d".repeat(5000));
        assertThat(matcher.matches(), is(true));
    }
}
