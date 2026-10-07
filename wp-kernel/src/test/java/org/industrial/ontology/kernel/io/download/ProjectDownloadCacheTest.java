package org.industrial.ontology.kernel.io.download;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.download.ProjectDownloadCache_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Apr 2017
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectDownloadCacheTest {

    private static final String THE_PROJECT_ID = "TheProjectId";

    private static final long REVISION_NUMBER = 33L;

    private ProjectDownloadCache cache;

    @Mock
    private ProjectDownloadCacheDirectorySupplier directorySupplier;

    @Mock
    private ProjectId projectId;

    @Mock
    private RevisionNumber revisionNumber;

    private DownloadFormat downloadFormat;

    private Path root;

    @BeforeEach
    public void setUp() throws Exception {
        when(projectId.getId()).thenReturn(THE_PROJECT_ID);
        when(revisionNumber.getValue()).thenReturn(REVISION_NUMBER);
        downloadFormat = DownloadFormat.RDF_XML;
        root = Paths.get("tmp");
        when(directorySupplier.get()).thenReturn(root);
        cache = new ProjectDownloadCache(directorySupplier);
    }

    @Test
    public void shouldResolvePath() {
        Path path = cache.getCachedDownloadPath(projectId, revisionNumber, downloadFormat);
        Path expectedPath = root.resolve(THE_PROJECT_ID).resolve(THE_PROJECT_ID + "-R" + REVISION_NUMBER + "." + downloadFormat.getExtension() + ".zip");
        assertThat(path, is(expectedPath));
    }
}
