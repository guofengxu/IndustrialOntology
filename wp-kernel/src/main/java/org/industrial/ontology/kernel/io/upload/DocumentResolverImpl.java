package org.industrial.ontology.kernel.io.upload;



import org.industrial.ontology.domain.upload.DocumentId;
import javax.annotation.Nonnull;

import java.io.File;
import java.nio.file.Path;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.upload.DocumentResolverImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public class DocumentResolverImpl implements DocumentResolver {


    @Nonnull
    private final File uploadsDirectory;

    public DocumentResolverImpl(@Nonnull File uploadsDirectory) {
        this.uploadsDirectory = checkNotNull(uploadsDirectory);
    }

    @Override
    public Path resolve(@Nonnull DocumentId documentId) {
        return uploadsDirectory.toPath().resolve(documentId.getDocumentId());
    }
}
