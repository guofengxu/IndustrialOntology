package org.industrial.ontology.kernel.io.upload;



import org.industrial.ontology.kernel.util.ZipInputStreamChecker;

import java.io.File;
import java.io.IOException;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.UploadedProjectSourcesExtractor}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public class UploadedProjectSourcesExtractor implements RawProjectSourcesExtractor {

    private ZipInputStreamChecker zipInputStreamChecker;

    private ZipArchiveProjectSourcesExtractor zipArchiveProjectSourcesExtractor;

    private SingleDocumentProjectSourcesExtractor singleDocumentProjectSourcesExtractor;

    public UploadedProjectSourcesExtractor(ZipInputStreamChecker zipInputStreamChecker,
                                           ZipArchiveProjectSourcesExtractor zipArchiveProjectSourcesExtractor,
                                           SingleDocumentProjectSourcesExtractor
                                                   singleDocumentProjectSourcesExtractor) {
        this.zipInputStreamChecker = zipInputStreamChecker;
        this.zipArchiveProjectSourcesExtractor = zipArchiveProjectSourcesExtractor;
        this.singleDocumentProjectSourcesExtractor = singleDocumentProjectSourcesExtractor;
    }

    @Override
    public RawProjectSources extractProjectSources(File inputFile) throws IOException {
        if (zipInputStreamChecker.isZipFile(inputFile)) {
            return zipArchiveProjectSourcesExtractor.extractProjectSources(inputFile);
        }
        else {
            return singleDocumentProjectSourcesExtractor.extractProjectSources(inputFile);
        }
    }
}
