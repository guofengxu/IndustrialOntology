package org.industrial.ontology.kernel.util;



import org.apache.commons.io.IOUtils;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.ZipFileExtractor}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public class ZipFileExtractor {

    public ZipFileExtractor() {
    }

    /**
     * Extracts every entry of {@code zip} below {@code outputDirectory}.
     * <p>
     * Unlike the legacy version, the archive is closed afterwards (it stayed open, which locks uploads on Windows)
     * and entries whose path would resolve outside {@code outputDirectory} ("zip slip") are rejected, because the
     * archives are user uploads.
     *
     * @throws IOException if reading fails or an entry escapes the output directory
     */
    public void extractFileToDirectory(File zip, File outputDirectory) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> zipEntryEnumeration = zipFile.entries();
            while (zipEntryEnumeration.hasMoreElements()) {
                extractZipEntry(outputDirectory, zipFile, zipEntryEnumeration.nextElement());
            }
        }
    }

    private void extractZipEntry(File outputDirectory, ZipFile zipFile, ZipEntry entry) throws IOException {
        String entryName = entry.getName();
        File entryFile = new File(outputDirectory, entryName);
        if (!entryFile.getCanonicalPath().startsWith(outputDirectory.getCanonicalPath() + File.separator)) {
            throw new IOException("Zip entry is outside of the target directory: " + entryName);
        }
        if (entryName.endsWith("/")) {
            entryFile.mkdirs();
        }
        else {
            File entryParentFile = entryFile.getParentFile();
            entryParentFile.mkdirs();
            try (BufferedOutputStream entryOutputStream = new BufferedOutputStream(new FileOutputStream(entryFile));
                 InputStream entryInputStream = zipFile.getInputStream(entry)) {
                IOUtils.copy(entryInputStream, entryOutputStream);
            }
        }
    }
}
