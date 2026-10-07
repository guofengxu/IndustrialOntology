package org.industrial.ontology.kernel.object;

import org.industrial.ontology.domain.object.SWRLAtomSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.SWRLAtom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
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
 * Ported from {@code edu.stanford.bmir.protege.web.server.object.SWRLAtomSelector_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SWRLAtomSelectorTest {

    public static final int BEFORE = -1;

    public static final int AFTER = 1;

    private SWRLAtomSelector selector;

    @Mock
    private Comparator<SWRLAtom> comparator;

    @Mock
    private SWRLAtom atom1, atom2;

    @BeforeEach
    public void setUp() throws Exception {
        selector = new SWRLAtomSelector(comparator);
    }

    @Test
    public void shouldReturnAbsent() {
        Iterable<SWRLAtom> input = Collections.emptyList();
        assertThat(selector.selectOne(input), is(Optional.empty()));
    }

    @Test
    public void shouldReturnSingleAtom() {
        List<SWRLAtom> input = Collections.singletonList(atom1);
        assertThat(selector.selectOne(input), is(Optional.of(atom1)));
    }

    @Test
    public void shouldReturnTheSmallestAtom() {
        when(comparator.compare(atom1, atom2)).thenReturn(BEFORE);
        List<SWRLAtom> input = Arrays.asList(atom2, atom1);
        assertThat(selector.selectOne(input), is(Optional.of(atom1)));
    }
}
