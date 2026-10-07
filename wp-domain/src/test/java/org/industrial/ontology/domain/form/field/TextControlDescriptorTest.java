package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.lang.LanguageMap;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.TextControlDescriptor_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TextControlDescriptorTest {

    private TextControlDescriptor textFieldDescriptor;

    private LanguageMap placeholder = LanguageMap.of("en", "The placeholder");

    private StringType stringType = StringType.SIMPLE_STRING;

    private LineMode lineMode = LineMode.SINGLE_LINE;

    private String pattern = "The pattern";

    private LanguageMap patternViolationErrorMessage = LanguageMap.of("en", "The patternViolationErrorMessage");

    @BeforeEach
    public void setUp() throws Exception {
        textFieldDescriptor = new TextControlDescriptor(placeholder, stringType, lineMode, pattern, patternViolationErrorMessage);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_placeholder_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new TextControlDescriptor(null, stringType, lineMode, pattern, patternViolationErrorMessage);
        });
    }

    @Test
    public void shouldReturnSupplied_placeholder() {
        assertThat(textFieldDescriptor.getPlaceholder(), is(this.placeholder));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_stringType_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new TextControlDescriptor(placeholder, null, lineMode, pattern, patternViolationErrorMessage);
        });
    }

    @Test
    public void shouldReturnSupplied_stringType() {
        assertThat(textFieldDescriptor.getStringType(), is(this.stringType));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_lineMode_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new TextControlDescriptor(placeholder, stringType, null, pattern, patternViolationErrorMessage);
        });
    }

    @Test
    public void shouldReturnSupplied_lineMode() {
        assertThat(textFieldDescriptor.getLineMode(), is(this.lineMode));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_pattern_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new TextControlDescriptor(placeholder, stringType, lineMode, null, patternViolationErrorMessage);
        });
    }

    @Test
    public void shouldReturnSupplied_pattern() {
        assertThat(textFieldDescriptor.getPattern(), is(this.pattern));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_patternViolationErrorMessage_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new TextControlDescriptor(placeholder, stringType, lineMode, pattern, null);
        });
    }

    @Test
    public void shouldReturnSupplied_patternViolationErrorMessage() {
        assertThat(textFieldDescriptor.getPatternViolationErrorMessage(), is(this.patternViolationErrorMessage));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(textFieldDescriptor, is(textFieldDescriptor));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(textFieldDescriptor.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(textFieldDescriptor, is(new TextControlDescriptor(placeholder, stringType, lineMode, pattern, patternViolationErrorMessage)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_placeholder() {
        assertThat(textFieldDescriptor, is(not(new TextControlDescriptor(LanguageMap.of("en", "String-1a3f1304-8c2f-48b2-8c60-3407b27d579f"), stringType, lineMode, pattern, patternViolationErrorMessage))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_stringType() {
        assertThat(textFieldDescriptor, is(not(new TextControlDescriptor(placeholder, StringType.LANG_STRING, lineMode, pattern, patternViolationErrorMessage))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_lineMode() {
        assertThat(textFieldDescriptor, is(not(new TextControlDescriptor(placeholder, stringType, LineMode.MULTI_LINE, pattern, patternViolationErrorMessage))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_pattern() {
        assertThat(textFieldDescriptor, is(not(new TextControlDescriptor(placeholder, stringType, lineMode, "String-ac3398da-4b52-48b5-a858-1f8571134db7", patternViolationErrorMessage))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_patternViolationErrorMessage() {
        assertThat(textFieldDescriptor, is(not(new TextControlDescriptor(placeholder, stringType, lineMode, pattern, LanguageMap.of("en", "String-1abd718d-0eb4-4530-b465-02aed8dd66a2")))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(textFieldDescriptor.hashCode(), is(new TextControlDescriptor(placeholder, stringType, lineMode, pattern, patternViolationErrorMessage).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(textFieldDescriptor.toString(), Matchers.startsWith("TextControlDescriptor"));
    }

    @Test
    public void should_getAssociatedType() {
        assertThat(textFieldDescriptor.getAssociatedType(), is("TEXT"));
    }

    @Test
    public void should_getType() {
        assertThat(TextControlDescriptor.getType(), is("TEXT"));
    }
}
