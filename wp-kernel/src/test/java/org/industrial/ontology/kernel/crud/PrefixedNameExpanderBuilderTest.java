package org.industrial.ontology.kernel.crud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.vocab.Namespaces;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.PrefixedNameExpanderBuilderTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 16/04/2014
 */
public class PrefixedNameExpanderBuilderTest {

    private PrefixedNameExpander.Builder builder;

    @BeforeEach
    public void setUp() throws Exception {
        builder = PrefixedNameExpander.builder();
    }

    @Test
    public void shouldThrowNullPointerExceptionForNullPrefixName() {
        assertThrows(NullPointerException.class, () -> {
            builder.withPrefixNamePrefix(null, "x");
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionForNullPrefix() {
        assertThrows(NullPointerException.class, () -> {
            builder.withPrefixNamePrefix("x:", null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForNonColonizedPrefixName() {
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withPrefixNamePrefix("x", "y");
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForDoubleColonizedPrefixName() {
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withPrefixNamePrefix("x::", "y");
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForStartEndDoubleColonizedPrefixName() {
        assertThrows(IllegalArgumentException.class, () -> {
            builder.withPrefixNamePrefix(":x:", "y");
        });
    }

    @Test
    public void shouldAddNamespaces() {
        builder.withNamespaces(Namespaces.values());
    }
}
