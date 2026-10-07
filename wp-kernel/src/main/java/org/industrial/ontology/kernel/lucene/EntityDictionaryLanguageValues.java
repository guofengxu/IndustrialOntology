package org.industrial.ontology.kernel.lucene;

import com.google.common.collect.ImmutableSetMultimap;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Map;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityDictionaryLanguageValues}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-06
 */
public record EntityDictionaryLanguageValues(@Nonnull OWLEntity entity, @Nonnull ImmutableSetMultimap<DictionaryLanguage, String> values) {

    public EntityDictionaryLanguageValues {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(values, "Null values");
    }

    public static EntityDictionaryLanguageValues get(@Nonnull OWLEntity entity, @Nonnull ImmutableSetMultimap<DictionaryLanguage, String> values) {
        return new EntityDictionaryLanguageValues(entity, values);
    }

    /**
     * Reduces this map of dictionary language values to a map of entity short forms.  Recalling that an entity shortform
     * map only has one value per dictionary language, only the first
     * value for each dictionary language in this map will be kept in the result.
     * @return The {@link EntityShortForms} for this {@link EntityDictionaryLanguageValues} object.
     */
    @Nonnull
    public EntityShortForms reduceToEntityShortForms() {
        var entity = getEntity();
        var map = getValues().entries().stream().collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue, (firstValue, secondValue) -> firstValue));
        return EntityShortForms.get(entity, map);
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    @Nonnull
    public ImmutableSetMultimap<DictionaryLanguage, String> getValues() {
        return values;
    }
}
