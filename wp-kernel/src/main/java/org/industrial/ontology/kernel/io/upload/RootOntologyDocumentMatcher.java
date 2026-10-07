package org.industrial.ontology.kernel.io.upload;



import java.io.File;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.RootOntologyDocumentMatcherImpl}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public class RootOntologyDocumentMatcher implements RootOntologyDocumentFileMatcher {

    public static final String ROOT_ONTOLOGY_DOCUMENT_FILE_NAME = "root-ontology.owl";

    public RootOntologyDocumentMatcher() {
    }

    @Override
    public boolean isRootOntologyDocument(File file) {
        return file.getName().equals(ROOT_ONTOLOGY_DOCUMENT_FILE_NAME);
    }

    @Override
    public String getErrorMessage() {
        return "The zip file should contain one ontology document named root-ontology.owl";
    }
}
