package org.industrial.ontology.kernel.project;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import java.io.File;
import java.util.Collection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import org.industrial.ontology.kernel.io.upload.RawProjectSources;
import org.industrial.ontology.kernel.io.upload.SingleDocumentProjectSourcesExtractor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.SingleDocumentProjectSourcesExtractor_TestCase}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SingleDocumentProjectSourcesExtractorTest {

    @Mock
    private File input;

    @Test
    public void shouldExtractProvidedDocument() {
        // Given
        SingleDocumentProjectSourcesExtractor extractor = new SingleDocumentProjectSourcesExtractor();
        // When
        RawProjectSources projectSources = extractor.extractProjectSources(input);
        // Then
        Collection<OWLOntologyDocumentSource> documentSources = projectSources.getDocumentSources();
        assertThat(documentSources, hasSize(1));
    }
}
