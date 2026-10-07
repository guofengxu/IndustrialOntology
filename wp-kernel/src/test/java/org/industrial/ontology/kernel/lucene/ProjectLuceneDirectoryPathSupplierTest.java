package org.industrial.ontology.kernel.lucene;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ProjectLuceneDirectoryPathSupplier_TestCase}.
 */
public class ProjectLuceneDirectoryPathSupplierTest {

    private ProjectLuceneDirectoryPathSupplier pathSupplier;

    private Path baseDirectoryPath = Path.of("/tmp", "lucene");

    private ProjectId projectId = ProjectId.get("12345678-1234-1234-1234-123456789abc");

    @BeforeEach
    public void setUp() throws Exception {
        pathSupplier = new ProjectLuceneDirectoryPathSupplier(baseDirectoryPath, projectId);
    }

    @Test
    public void shouldGetPathForProject() {
        Path path = pathSupplier.get();
        assertThat(path, is(equalTo(Path.of("/tmp", "lucene", projectId.getId()))));
    }
}
