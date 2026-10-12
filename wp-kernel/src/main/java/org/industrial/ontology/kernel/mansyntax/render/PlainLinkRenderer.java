package org.industrial.ontology.kernel.mansyntax.render;

import org.industrial.ontology.kernel.render.HtmlBuilder;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.PlainLinkRenderer}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 21/02/2014
 */
public class PlainLinkRenderer implements LinkRendererPlugin {

    @Override
    public boolean isRenderableAsLink(String link) {
        // Only http(s) URLs; the legacy check accepted any scheme that starts with "http"
        return link.startsWith("http://") || link.startsWith("https://");
    }

    @Override
    public LinkInfo renderLink(String link) {
        return new LinkInfo(link, new HtmlBuilder().appendEscaped(link).toSafeHtml());
    }
}
