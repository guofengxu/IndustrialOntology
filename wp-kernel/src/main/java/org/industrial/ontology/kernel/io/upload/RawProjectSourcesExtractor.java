package org.industrial.ontology.kernel.io.upload;



import java.io.File;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.RawProjectSourcesExtractor}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public interface RawProjectSourcesExtractor {

    RawProjectSources extractProjectSources(File inputFile) throws IOException;
}
