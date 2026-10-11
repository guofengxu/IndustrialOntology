package org.industrial.ontology.kernel.io.upload;

import java.io.FileNotFoundException;

/**
 * An uploaded zip archive does not contain the root ontology document that {@link RootOntologyDocumentFileMatcher}
 * looks for. The message is the matcher's error message, which tells the user how to fix the archive.
 * <p>
 * The legacy extractor threw a plain {@link FileNotFoundException}; this subclass lets callers tell the user's mistake
 * from a file the server could not create.
 */
public class RootOntologyDocumentNotFoundException extends FileNotFoundException {

    private static final long serialVersionUID = 1L;

    public RootOntologyDocumentNotFoundException(String message) {
        super(message);
    }
}
