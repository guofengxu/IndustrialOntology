package org.industrial.ontology.kernel.mansyntax.render;

import org.industrial.ontology.kernel.render.HtmlBuilder;




/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.MarkdownLiteralRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/02/2014
 */
public class MarkdownLiteralRenderer implements LiteralRenderer {

    public MarkdownLiteralRenderer() {
    }

    @Override
    public void renderLiteral(String literal, StringBuilder stringBuilder) {
        // TODO:  Won't work until we move to Java 7
        // The legacy renderer appended the literal as it was, so a literal could put markup and scripts into the
        // HTML descriptions that are shown to everyone who may view the project.
        stringBuilder.append(new HtmlBuilder().appendEscaped(literal).toSafeHtml());
//        try {
//            stringBuilder.append(Processor.process(literal));
//            Markdown4jProcessor markdown4jProcessor = new Markdown4jProcessor();
//            String rendering = markdown4jProcessor.process(literal);
//            stringBuilder.append(rendering.trim());
//        } catch (IOException e) {
//            stringBuilder.append(literal);
//        }
    }
}
