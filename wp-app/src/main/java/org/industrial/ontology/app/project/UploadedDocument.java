package org.industrial.ontology.app.project;

import org.industrial.ontology.domain.upload.DocumentId;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An uploaded file in the uploads directory, ready to create a project from.
 *
 * @param fileName the name that the client gave the file
 * @param size     the size in bytes
 */
public record UploadedDocument(@Nonnull DocumentId documentId, @Nonnull String fileName, long size) {

    public UploadedDocument {
        checkNotNull(documentId);
        checkNotNull(fileName);
    }
}
