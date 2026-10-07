package org.industrial.ontology.kernel.io.upload;



import org.industrial.ontology.domain.upload.DocumentId;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.upload.DocumentResolver}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public interface DocumentResolver {

    /**
     * Resolve the specified {@link DocumentId} to a path on the server.
     */
    Path resolve(@Nonnull DocumentId documentId);
}
