package org.industrial.ontology.kernel.shortform;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.DefaultShortFormAnnotationPropertyIRIs_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/01/15
 */
public class DefaultShortFormAnnotationPropertyIRIsTest {

    private ImmutableList<IRI> defaultLabellingIRIs;

    @BeforeEach
    public void setUp() throws Exception {
        defaultLabellingIRIs = DefaultShortFormAnnotationPropertyIRIs.asImmutableList();
    }

    @Test
    public void shouldContainRDFSLabel() {
        assertThat(defaultLabellingIRIs, hasItem(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
    }

    @Test
    public void shouldContainSKOSPrefLabel() {
        assertThat(defaultLabellingIRIs, hasItem(SKOSVocabulary.PREFLABEL.getIRI()));
    }
}
