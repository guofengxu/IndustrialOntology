package org.industrial.ontology.kernel.io.upload;



import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import java.io.FileNotFoundException;
import java.io.Reader;

import java.io.UncheckedIOException;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ProjectInputSource}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 3 Apr 2018
 */
public class ProjectInputSource implements OWLOntologyDocumentSource {

    private static final int BUFFER_SIZE = (8 * 1024 * 1024) + 100;

    private File rootOntologyDocument;


    public ProjectInputSource(@Nonnull File rootOntologyDocument) {
        this.rootOntologyDocument = checkNotNull(rootOntologyDocument);
    }

    @Override
    public boolean isReaderAvailable() {
        return false;
    }

    @Nonnull
    @Override
    public Reader getReader() {
        throw new RuntimeException("Reader Not Available");
    }

    @Override
    public boolean isInputStreamAvailable() {
        return true;
    }

    @Nonnull
    @Override
    public InputStream getInputStream() {
        try {
            return new BufferedInputStream(new FileInputStream(rootOntologyDocument),
                                           BUFFER_SIZE);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nonnull
    @Override
    public IRI getDocumentIRI() {
        return IRI.create(rootOntologyDocument.toURI());
    }

    @Nullable
    @Override
    public OWLDocumentFormat getFormat() {
        return new BinaryOWLOntologyDocumentFormat();
    }

    @Override
    public boolean isFormatKnown() {
        return true;
    }

    @Override
    public String getMIMEType() {
        throw new RuntimeException("MIME Type is Not Available");
    }

    @Override
    public boolean isMIMETypeKnown() {
        return false;
    }

    @Override
    public void setAcceptHeaders(String headers) {

    }

    @Override
    public Optional<String> getAcceptHeaders() {
        return Optional.empty();
    }
}
