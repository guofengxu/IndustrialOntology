package org.industrial.ontology.kernel.project;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.project.PrefixDeclaration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

import org.industrial.ontology.kernel.api.project.BuiltInPrefixDeclarations;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.BuiltInPrefixDeclarationsLoader_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-04-30
 */
public class BuiltInPrefixDeclarationsLoaderTest {

    @Test
    public void shouldLoadBuiltInDeclarationsFromClassPath() {
        OverridableFileFactory fileFactory = new OverridableFileFactory(new File("/tmp"));
        BuiltInPrefixDeclarationsLoader manager = new BuiltInPrefixDeclarationsLoader(fileFactory);
        BuiltInPrefixDeclarations decls = manager.getBuiltInPrefixDeclarations();
        ImmutableList<PrefixDeclaration> prefixDeclarations = decls.getPrefixDeclarations();
        assertThat(prefixDeclarations.isEmpty(), Matchers.is(false));
    }
}
