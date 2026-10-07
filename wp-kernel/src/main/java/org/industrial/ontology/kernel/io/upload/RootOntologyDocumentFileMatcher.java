package org.industrial.ontology.kernel.io.upload;



import java.io.File;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.RootOntologyDocumentFileMatcher}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public interface RootOntologyDocumentFileMatcher {

    boolean isRootOntologyDocument(File file);

    String getErrorMessage();
}
