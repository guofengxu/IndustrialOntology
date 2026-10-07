package org.industrial.ontology.domain.lang;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LangTag}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public record LangTag(@Nonnull String languageCode) {

    public LangTag {
        Objects.requireNonNull(languageCode, "Null languageCode");
    }

    @Nonnull
    public static LangTag get(@Nonnull String langTag) {
        return new LangTag(langTag.trim().toLowerCase());
    }

    public String format() {
        return LanguageTagFormatter.format(getLanguageCode());
    }

    @Nonnull
    public String getLanguageCode() {
        return languageCode;
    }
}
