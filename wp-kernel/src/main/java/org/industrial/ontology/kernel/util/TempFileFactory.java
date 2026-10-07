package org.industrial.ontology.kernel.util;



import java.io.File;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.TempFileFactory}.
 * <p>
* @author Matthew Horridge,
*         Stanford University,
*         Bio-Medical Informatics Research Group
*         Date: 18/02/2014
*/
public interface TempFileFactory {

    /**
     * Creates a fresh, empty temporary directory.
     * @return The directory.
     */
    File createTempDirectory() throws IOException;
}
