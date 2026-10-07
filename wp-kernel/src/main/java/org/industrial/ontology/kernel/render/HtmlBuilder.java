package org.industrial.ontology.kernel.render;

/**
 * Builds an HTML fragment from escaped text and trusted markup.
 * <p>
 * The legacy renderers used GWT's {@code SafeHtmlBuilder}; since GWT's {@code SafeHtml} type maps to plain
 * {@code String} in the port, this keeps the same calls and escaping rules ({@code & < > " '}) while
 * {@link #toSafeHtml()} returns the markup itself.
 */
public final class HtmlBuilder {

    private final StringBuilder sb = new StringBuilder();

    /** Appends {@code text} with HTML special characters escaped. */
    public HtmlBuilder appendEscaped(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return this;
    }

    /** Appends markup that the caller guarantees to be well formed and safe. */
    public HtmlBuilder appendHtmlConstant(String html) {
        sb.append(html);
        return this;
    }

    public String toSafeHtml() {
        return sb.toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
