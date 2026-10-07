package org.industrial.ontology.domain.diff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.io.Serializable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.diff.DiffElement_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 29/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DiffElementTest<D extends Serializable, E extends Serializable> {

    private DiffElement<D, E> diffElement;

    private DiffElement<D, E> otherDiffElement;

    @Mock
    private D sourceDocument;

    @Mock
    private E lineElement;

    private DiffOperation diffOperation = DiffOperation.ADD;

    @BeforeEach
    public void setUp() throws Exception {
        diffElement = new DiffElement<>(diffOperation, sourceDocument, lineElement);
        otherDiffElement = new DiffElement<>(diffOperation, sourceDocument, lineElement);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_DiffOperation_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new DiffElement<>(null, sourceDocument, lineElement);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_SourceDocument_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new DiffElement<>(diffOperation, null, lineElement);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_LineElement_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new DiffElement<>(diffOperation, sourceDocument, null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(diffElement, is(equalTo(diffElement)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(diffElement, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(diffElement, is(equalTo(otherDiffElement)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(diffElement.hashCode(), is(otherDiffElement.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(diffElement.toString(), startsWith("DiffElement"));
    }

    @Test
    public void shouldReturnSuppliedDiffOperation() {
        assertThat(diffElement.getDiffOperation(), is(diffOperation));
    }

    @Test
    public void shouldReturnSuppliedSourceDocument() {
        assertThat(diffElement.getSourceDocument(), is(sourceDocument));
    }

    @Test
    public void shouldReturnSuppliedLineElement() {
        assertThat(diffElement.getLineElement(), is(lineElement));
    }
}
