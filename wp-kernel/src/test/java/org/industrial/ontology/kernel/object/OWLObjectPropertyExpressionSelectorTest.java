package org.industrial.ontology.kernel.object;

import org.industrial.ontology.domain.object.OWLObjectPropertyExpressionSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.Optional;
import java.util.Arrays;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.object.OWLObjectPropertyExpressionSelector_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OWLObjectPropertyExpressionSelectorTest {

    public static final int BEFORE = -1;

    public static final int AFTER = 1;

    private OWLObjectPropertyExpressionSelector selector;

    @Mock
    private Comparator<OWLObjectPropertyExpression> propertyComparator;

    @Mock
    private OWLObjectProperty property1, property2;

    @Mock
    private OWLObjectPropertyExpression propertyExpression1, propertyExpression2;

    @BeforeEach
    public void setUp() throws Exception {
        selector = new OWLObjectPropertyExpressionSelector(propertyComparator);
    }

    @Test
    public void shouldNotSelectAnythingForEmptyList() {
        assertThat(selector.selectOne(Collections.emptyList()), is(Optional.empty()));
    }

    @Test
    public void shouldSelectAbsentForNoPropertyName() {
        List<OWLObjectPropertyExpression> input = Arrays.asList(propertyExpression1, propertyExpression2);
        assertThat(selector.selectOne(input), is(Optional.empty()));
    }

    @Test
    public void shouldSelectSingleOWLObjectProperty() {
        List<OWLObjectPropertyExpression> input = Arrays.asList(propertyExpression1, propertyExpression2, property2);
        assertThat(selector.selectOne(input), is(Optional.empty()));
    }

    @Test
    public void shouldSelectSmallerOWLObjectProperty() {
        List<OWLObjectPropertyExpression> input = Arrays.asList(property2, property1, propertyExpression1);
        assertThat(selector.selectOne(input), is(Optional.empty()));
    }
}
