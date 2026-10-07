package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.function.Supplier;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ProjectLuceneDirectoryPathSupplier}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public class ProjectLuceneDirectoryPathSupplier implements Supplier<Path> {

    @Nonnull
    private final Path luceneIndexesDirectory;

    @Nonnull
    private final ProjectId projectId;

    public ProjectLuceneDirectoryPathSupplier(@Nonnull @LuceneIndexesDirectory Path luceneIndexesDirectory,
                                              @Nonnull ProjectId projectId) {
        this.luceneIndexesDirectory = checkNotNull(luceneIndexesDirectory);
        this.projectId = checkNotNull(projectId);
    }

    @Override
    public Path get() {
        return luceneIndexesDirectory.resolve(projectId.getId());
    }
}
