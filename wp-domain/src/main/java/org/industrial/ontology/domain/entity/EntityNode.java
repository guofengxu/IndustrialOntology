package org.industrial.ontology.domain.entity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageData;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.watches.Watch;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Collections;
import java.util.Objects;
import java.util.Collection;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityNode}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 28 Nov 2017
 */
public record EntityNode(@Nonnull OWLEntity entity, @Nonnull String browserText, ImmutableSet<Tag> tags, boolean deprecated, ImmutableSet<Watch> watches, int openCommentCount, @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms) implements Serializable, Comparable<EntityNode> {

    public EntityNode {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(browserText, "Null browserText");
        Objects.requireNonNull(tags, "Null tags");
        Objects.requireNonNull(watches, "Null watches");
        Objects.requireNonNull(shortForms, "Null shortForms");
    }

    private static final boolean NOT_DEPRECATED = false;

    private static final ImmutableSet<Watch> NO_WATCHES = ImmutableSet.of();

    private static final int NO_OPEN_COMMENTS = 0;

    private static final ImmutableSet<Tag> NO_ENTITY_TAGS = ImmutableSet.of();

    @Nonnull
    public static EntityNode get(@Nonnull OWLEntity entity, @Nonnull String browserText, @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms, boolean deprecated, @Nonnull Set<Watch> watches, int openCommentCount, Collection<Tag> tags) {
        return new EntityNode(entity, browserText, ImmutableSet.copyOf(tags), deprecated, ImmutableSet.copyOf(watches), openCommentCount, shortForms);
    }

    /**
     * Gets a basic {@link EntityNode} for the specified {@link OWLEntityData}.  The
     * node will be rendered without any indications of deprecation status,
     * watches, open comments count, and entity tags
     * @param entityData The entity data
     * @return A basic rendering of the specified entity data
     */
    @Nonnull
    public static EntityNode getFromEntityData(@Nonnull OWLEntityData entityData) {
        return get(entityData.getEntity(), entityData.getBrowserText(), entityData.getShortForms(), NOT_DEPRECATED, NO_WATCHES, NO_OPEN_COMMENTS, NO_ENTITY_TAGS);
    }

    @Nonnull
    public String getText() {
        return getBrowserText();
    }

    public String getText(@Nonnull DictionaryLanguageData prefLang) {
        return getText(Collections.singletonList(prefLang.getDictionaryLanguage()), getBrowserText());
    }

    public String getText(@Nonnull List<DictionaryLanguage> prefLang, String defaultText) {
        return prefLang.stream().map(language -> getShortForms().get(language)).filter(Objects::nonNull).findFirst().orElse(defaultText);
    }

    public OWLEntityData getEntityData() {
        return DataFactory.getOWLEntityData(getEntity(), getShortForms(), isDeprecated());
    }

    @Override
    public int compareTo(EntityNode o) {
        return this.getBrowserText().compareToIgnoreCase(o.getBrowserText());
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }

    @Nonnull
    public String getBrowserText() {
        return browserText;
    }

    public ImmutableSet<Tag> getTags() {
        return tags;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public ImmutableSet<Watch> getWatches() {
        return watches;
    }

    public int getOpenCommentCount() {
        return openCommentCount;
    }

    @Nonnull
    public ImmutableMap<DictionaryLanguage, String> getShortForms() {
        return shortForms;
    }
}
