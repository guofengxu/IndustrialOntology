package org.industrial.ontology.kernel.util;




import java.io.File;
import java.io.IOException;
import java.util.UUID;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.TempFileFactoryImpl}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
public class TempFileFactoryImpl implements TempFileFactory {

    public TempFileFactoryImpl() {
    }

    @Override
    public File createTempDirectory() throws IOException {
        File systemTempDirectory = new File("/tmp");
        File result = new File(systemTempDirectory, "tmp-" + UUID.randomUUID().toString());
        result.mkdirs();
        return result;
    }
}
