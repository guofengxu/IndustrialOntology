package org.industrial.ontology.domain.search;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntityNameMatcher_TestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/11/2013
 */
public class EntityNameMatcherTest {

    @Test
    public void shouldThrowNullPointerExceptionForNullSearchString() {
        assertThrows(NullPointerException.class, () -> {
            new EntityNameMatcher(null);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionForNullEntityName() {
        assertThrows(NullPointerException.class, () -> {
            EntityNameMatcher matcher = new EntityNameMatcher("");
            matcher.findIn(null);
        });
    }

    @Test
    public void shouldReturnExactMatchOfNonQuotedString() {
        EntityNameMatcher matcher = new EntityNameMatcher("abc");
        Optional<EntityNameMatchResult> result = matcher.findIn("abc");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(0, resultValue.getStart());
        assertEquals(3, resultValue.getEnd());
        assertEquals(EntityNameMatchType.EXACT_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldReturnExactMatchOfQuotedString() {
        EntityNameMatcher matcher = new EntityNameMatcher("abc");
        Optional<EntityNameMatchResult> result = matcher.findIn("'abc'");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(1, resultValue.getStart());
        assertEquals(4, resultValue.getEnd());
        assertEquals(EntityNameMatchType.EXACT_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldReturnWordMatchOfEntityNameWithSpaced() {
        EntityNameMatcher matcher = new EntityNameMatcher("abc");
        Optional<EntityNameMatchResult> result = matcher.findIn("abc def");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(0, resultValue.getStart());
        assertEquals(3, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldReturnWordMatchOfEntityNameWithCamelCaseWord() {
        EntityNameMatcher matcher = new EntityNameMatcher("abc");
        Optional<EntityNameMatchResult> result = matcher.findIn("abcDef");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(0, resultValue.getStart());
        assertEquals(3, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldReturnWordMatchOfEntityNameWithCamelCaseWordHavingCommonSubstringPrefix() {
        EntityNameMatcher matcher = new EntityNameMatcher("bc");
        Optional<EntityNameMatchResult> result = matcher.findIn("abcBc");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(3, resultValue.getStart());
        assertEquals(5, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldFindWordAfterPrefixName() {
        EntityNameMatcher matcher = new EntityNameMatcher("ab");
        Optional<EntityNameMatchResult> result = matcher.findIn("ab:ab");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(3, resultValue.getStart());
        assertEquals(5, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldFindEmptyStringAtStart() {
        EntityNameMatcher matcher = new EntityNameMatcher("");
        Optional<EntityNameMatchResult> result = matcher.findIn("ab");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(0, resultValue.getStart());
        assertEquals(0, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_PREFIX_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.NOT_IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldFindEmptyStringBeforePrefixSeparator() {
        EntityNameMatcher matcher = new EntityNameMatcher("");
        Optional<EntityNameMatchResult> result = matcher.findIn("ab:ab");
        assertEquals(true, result.isPresent());
        EntityNameMatchResult resultValue = result.get();
        assertEquals(0, resultValue.getStart());
        assertEquals(0, resultValue.getEnd());
        assertEquals(EntityNameMatchType.WORD_PREFIX_MATCH, resultValue.getMatchType());
        assertEquals(PrefixNameMatchType.IN_PREFIX_NAME, resultValue.getPrefixNameMatchType());
    }

    @Test
    public void shouldReturnAbsentForNonSubString() {
        EntityNameMatcher matcher = new EntityNameMatcher("a");
        Optional<EntityNameMatchResult> result = matcher.findIn("x:xXx");
        assertEquals(false, result.isPresent());
    }

    @Test
    public void shouldReturnAbsentForSearchingOnEmptyString() {
        EntityNameMatcher matcher = new EntityNameMatcher("a");
        Optional<EntityNameMatchResult> result = matcher.findIn("");
        assertEquals(false, result.isPresent());
    }
}
