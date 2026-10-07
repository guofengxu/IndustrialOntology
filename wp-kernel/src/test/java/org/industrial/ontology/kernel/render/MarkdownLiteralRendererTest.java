package org.industrial.ontology.kernel.render;



import org.industrial.ontology.kernel.mansyntax.render.MarkdownLiteralRenderer;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.MarkdownLiteralRendererTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 14/04/2014
 */
public class MarkdownLiteralRendererTest {

    @Test
    public void shouldRenderMarkdown() {
        // Temporary test case until we support markdown properly
        MarkdownLiteralRenderer literalRenderer = new MarkdownLiteralRenderer();
        StringBuilder builder = new StringBuilder();
        literalRenderer.renderLiteral("Title", builder);
        assertThat(builder.toString(), is(equalTo("Title")));
    }
}
