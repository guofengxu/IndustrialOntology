package org.industrial.ontology.kernel.shortform;

import com.google.common.base.Optional;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.WebProtegeOntologyIRIShortFormProvider_TestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/03/2014
 */
@SuppressWarnings("Guava")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class WebProtegeOntologyIRIShortFormProviderTest {

    private IRI iri;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private DefaultOntologyIdManager defaultOntologyIdManager;

    @BeforeEach
    public void setUp() throws Exception {
        iri = IRI.create("http://stuff.com/OntologyA");
        when(defaultOntologyIdManager.getDefaultOntologyId()).thenReturn(ontologyId);
        when(ontologyId.getOntologyIRI()).thenReturn(Optional.of(iri));
    }

    @Test
    public void shouldReturnStandardShortFormForRootOntology() {
        var sfp = new WebProtegeOntologyIRIShortFormProvider(defaultOntologyIdManager);
        var shortForm = sfp.getShortForm(ontologyId);
        assertThat(shortForm, is(equalTo("root-ontology")));
    }

    @Test
    public void shouldReturnStandardShortFormForRootOntologyIRI() {
        var sfp = new WebProtegeOntologyIRIShortFormProvider(defaultOntologyIdManager);
        var shortForm = sfp.getShortForm(iri);
        assertThat(shortForm, is(equalTo("root-ontology")));
    }
}
