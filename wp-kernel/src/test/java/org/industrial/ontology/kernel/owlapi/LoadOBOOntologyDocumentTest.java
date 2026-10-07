package org.industrial.ontology.kernel.owlapi;




import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.formats.OBODocumentFormat;
import java.io.IOException;

import java.net.URL;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.is;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.LoadOBOOntologyDocument_TestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 08/08/2014
 */
public class LoadOBOOntologyDocumentTest {


    private static final String SOURCE_DOCUMENT = "/ontologies/obo/go.fragment.obo";

    @Test
    public void shouldLoadOBOOntology() throws IOException, OWLOntologyCreationException {
        URL url = LoadOBOOntologyDocumentTest.class.getResource(SOURCE_DOCUMENT);
        OWLOntologyManager man = WebProtegeOWLManager.createOWLOntologyManager();
        OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI.create(url));
        OWLDocumentFormat format = man.getOntologyFormat(ont);
        assertThat(format instanceof OBODocumentFormat, is(true));
    }
}
