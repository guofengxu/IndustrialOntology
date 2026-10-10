package org.industrial.ontology.kernel.util;




import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.TempFileFactoryImpl}.
 * <p>
 * The legacy factory created its directories under a hard-coded {@code /tmp} (on Windows, {@code \tmp} of the
 * current drive) with default permissions; this one uses the JVM's temporary directory ({@code java.io.tmpdir}), and
 * on POSIX file systems only the owner can read the directory.
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
        return Files.createTempDirectory("tmp-").toFile();
    }
}
