package org.industrial.ontology.domain.util;

import java.nio.charset.StandardCharsets;

/**
 * URI escaping with the semantics of JavaScript {@code encodeURI}.
 * <p>
 * The legacy CRUD kits built example IRIs with GWT's {@code URL.encode}, which is {@code encodeURI}:
 * characters that are legal anywhere in a complete URI (including the reserved delimiters such as
 * {@code :/?#&=}) are kept, everything else is percent-encoded as UTF-8. {@link java.net.URLEncoder}
 * implements form encoding instead, so it would change existing prefixes.
 */
public final class UriEncoding {

    private static final String UNESCAPED = "-_.!~*'();/?:@&=+$,#";

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private UriEncoding() {
    }

    public static String encodeUri(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            int c = b & 0xFF;
            if (c < 0x80 && (Character.isLetterOrDigit(c) || UNESCAPED.indexOf(c) >= 0)) {
                sb.append((char) c);
            }
            else {
                sb.append('%').append(HEX[c >> 4]).append(HEX[c & 0xF]);
            }
        }
        return sb.toString();
    }
}
