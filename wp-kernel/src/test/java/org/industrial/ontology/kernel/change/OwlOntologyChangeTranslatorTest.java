package org.industrial.ontology.kernel.change;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OwlOntologyChangeTranslator_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OwlOntologyChangeTranslatorTest {

    private OwlOntologyChangeTranslator translator;

    @Mock
    private OwlOntologyChangeTranslatorVisitor visitor;

    @Mock
    private OWLOntologyChange change;

    @Mock
    private OntologyChange ontologyChange;

    @BeforeEach
    public void setUp() {
        translator = new OwlOntologyChangeTranslator(visitor);
        when(change.accept(visitor)).thenReturn(ontologyChange);
    }

    @Test
    public void shouldGetChange() {
        var result = translator.toOntologyChange(change);
        assertThat(result, is(ontologyChange));
    }
}
