package org.industrial.ontology.kernel.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.io.BufferedInputStream;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.ZipInputStreamChecker_TestCase}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ZipInputStreamCheckerTest {

    @Mock
    private BufferedInputStream bufferedInputStream;

    @Test
    public void shouldTestZipInputStream() throws IOException {
        when(bufferedInputStream.read()).thenReturn((int) 'P').thenReturn((int) 'K');
        ZipInputStreamChecker checker = new ZipInputStreamChecker();
        assertTrue(checker.isZipInputStream(bufferedInputStream));
    }

    @Test
    public void shouldTestNonZipInputStream() throws IOException {
        when(bufferedInputStream.read()).thenReturn((int) 'X').thenReturn((int) 'Y');
        ZipInputStreamChecker checker = new ZipInputStreamChecker();
        assertFalse(checker.isZipInputStream(bufferedInputStream));
    }
}
