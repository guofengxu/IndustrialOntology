package org.industrial.ontology.kernel.lucene;

import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityShortForms}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public record EntityShortForms(@Nonnull OWLEntity entity, @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms) {

    public EntityShortForms {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(shortForms, "Null shortForms");
    }

    @Nonnull
    public static EntityShortForms get(@Nonnull OWLEntity entity, @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms) {
        return new EntityShortForms(entity, shortForms);
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    @Nonnull
    public ImmutableMap<DictionaryLanguage, String> getShortForms() {
        return shortForms;
    }
}
