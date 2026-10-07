package org.industrial.ontology.kernel.project;

import org.industrial.ontology.kernel.util.ZipInputStreamChecker;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.io.File;
import java.io.IOException;
import org.industrial.ontology.kernel.io.upload.SingleDocumentProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.UploadedProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.ZipArchiveProjectSourcesExtractor;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.UploadedProjectSourcesExtractor_TestCase}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UploadedProjectSourcesExtractorTest {

    @Mock
    private ZipArchiveProjectSourcesExtractor zipArchiveProjectSourcesExtractor;

    @Mock
    private SingleDocumentProjectSourcesExtractor singleDocumentProjectSourcesExtractor;

    @Mock
    private ZipInputStreamChecker zipInputStreamChecker;

    @Mock
    private File inputFile;

    @Test
    public void shouldExtractSourcesFromZipFile() throws IOException {
        // Given
        when(zipInputStreamChecker.isZipFile(inputFile)).thenReturn(true);
        UploadedProjectSourcesExtractor extractor = new UploadedProjectSourcesExtractor(zipInputStreamChecker, zipArchiveProjectSourcesExtractor, singleDocumentProjectSourcesExtractor);
        // When
        extractor.extractProjectSources(inputFile);
        // Then
        verify(zipArchiveProjectSourcesExtractor, times(1)).extractProjectSources(inputFile);
    }

    @Test
    public void shouldExtractNonZipFileUsingSingleDocumentExtractor() throws IOException {
        // Given
        when(zipInputStreamChecker.isZipFile(inputFile)).thenReturn(false);
        UploadedProjectSourcesExtractor extractor = new UploadedProjectSourcesExtractor(zipInputStreamChecker, zipArchiveProjectSourcesExtractor, singleDocumentProjectSourcesExtractor);
        // When
        extractor.extractProjectSources(inputFile);
        // Then
        verify(singleDocumentProjectSourcesExtractor, times(1)).extractProjectSources(inputFile);
    }
}
