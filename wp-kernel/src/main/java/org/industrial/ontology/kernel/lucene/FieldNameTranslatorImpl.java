package org.industrial.ontology.kernel.lucene;



import javax.annotation.Nonnull;

import static java.util.stream.Collectors.joining;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;

import org.industrial.ontology.domain.lang.AnnotationAssertionPathDictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageVisitor;
import org.industrial.ontology.domain.lang.LocalNameDictionaryLanguage;
import org.industrial.ontology.domain.lang.OboIdDictionaryLanguage;
import org.industrial.ontology.domain.lang.PrefixedNameDictionaryLanguage;
import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.KEYWORD_FIELD_PREFIX;
import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.PREFIXED_NAME;
import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.TEXT_FIELD_PREFIX;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.FieldNameTranslatorImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public class FieldNameTranslatorImpl implements FieldNameTranslator {

    private static final DictionaryLanguageVisitor<String> languageVisitor = new DictionaryLanguageVisitor<>() {

        @Override
        public String visit(@Nonnull LocalNameDictionaryLanguage language) {
            return EntityDocumentFieldNames.LOCAL_NAME;
        }

        @Override
        public String visit(@Nonnull OboIdDictionaryLanguage language) {
            return EntityDocumentFieldNames.OBO_ID;
        }

        @Override
        public String visit(@Nonnull AnnotationAssertionDictionaryLanguage language) {
            return language.getLang() + "@" + language.getJsonAnnotationPropertyIri();
        }

        @Override
        public String visit(@Nonnull AnnotationAssertionPathDictionaryLanguage language) {
            var path = language.getAnnotationPropertyPath().stream().map(Object::toString).collect(joining(";"));
            return path + "@" + language.getLang();
        }

        @Override
        public String visit(@Nonnull PrefixedNameDictionaryLanguage language) {
            return PREFIXED_NAME;
        }
    };

    public FieldNameTranslatorImpl() {
    }

    @Nonnull
    @Override
    public String getNonTokenizedFieldName(@Nonnull DictionaryLanguage language) {
        return KEYWORD_FIELD_PREFIX + getFieldNameSuffix(language);
    }

    @Nonnull
    @Override
    public String getTokenizedFieldName(@Nonnull DictionaryLanguage language) {
        return TEXT_FIELD_PREFIX + getFieldNameSuffix(language);
    }

    @Nonnull
    private String getFieldNameSuffix(@Nonnull DictionaryLanguage language) {
        return language.accept(languageVisitor);
    }
}
