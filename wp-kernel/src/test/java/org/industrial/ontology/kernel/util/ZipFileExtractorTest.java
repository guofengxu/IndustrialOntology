package org.industrial.ontology.kernel.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.ZipFileExtractor_TestCase}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public class ZipFileExtractorTest {

    private byte[] bytes;

    @TempDir
    public Path folder;

    private File zipFile;

    @BeforeEach
    public void setUp() throws IOException {
        bytes = new byte[100];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }
        zipFile = Files.createFile(folder.resolve("zipFile")).toFile();
    }

    @Test
    public void shouldExtractZipFile() throws IOException {
        OutputStream out = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(out);
        ZipEntry entryA = new ZipEntry("/FileA");
        entryA.setSize(bytes.length);
        zipOutputStream.putNextEntry(entryA);
        zipOutputStream.write(bytes);
        ZipEntry entryB = new ZipEntry("/Directory/FileB");
        entryB.setSize(bytes.length);
        zipOutputStream.putNextEntry(entryB);
        zipOutputStream.write(bytes);
        ZipEntry entryC = new ZipEntry("/EmptyDirectory/");
        zipOutputStream.putNextEntry(entryC);
        zipOutputStream.close();
        ZipFileExtractor extractor = new ZipFileExtractor();
        File outputDirectory = Files.createDirectories(folder.resolve("out")).toFile();
        extractor.extractFileToDirectory(zipFile, outputDirectory);
        File extractedFileA = new File(outputDirectory, "FileA");
        assertTrue(extractedFileA.exists());
        assertEquals(extractedFileA.length(), 100);
        File extractedFileB = new File(outputDirectory, "/Directory/FileB");
        assertTrue(extractedFileB.exists());
        assertEquals(extractedFileB.length(), 100);
        File extractedDirectory = new File(outputDirectory, "Directory");
        assertTrue(extractedDirectory.exists());
        assertTrue(extractedDirectory.isDirectory());
        File emptyDirectory = new File(outputDirectory, "EmptyDirectory");
        assertTrue(emptyDirectory.exists());
        assertTrue(emptyDirectory.isDirectory());
    }

    @Test
    public void shouldRejectEntryThatEscapesTheOutputDirectory() throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipOutputStream.putNextEntry(new ZipEntry("../escaped.owl"));
            zipOutputStream.write(bytes);
        }
        File outputDirectory = Files.createDirectories(folder.resolve("out")).toFile();
        ZipFileExtractor extractor = new ZipFileExtractor();

        IOException e = assertThrows(IOException.class,
                                     () -> extractor.extractFileToDirectory(zipFile, outputDirectory));

        assertTrue(e.getMessage().contains("outside of the target directory"));
        assertFalse(folder.resolve("escaped.owl").toFile().exists());
    }
}
