package org.industrial.ontology.domain.entity;

import org.industrial.ontology.domain.search.SearchResultMatch;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityLookupResult}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/05/2012
 */
public record EntityLookupResult(@Nonnull DictionaryLanguage language, @Nonnull EntityNode entityNode, @Nonnull SearchResultMatch matchResult, @Nonnull String directLink) {

    public EntityLookupResult {
        Objects.requireNonNull(language, "Null language");
        Objects.requireNonNull(entityNode, "Null entityNode");
        Objects.requireNonNull(matchResult, "Null matchResult");
        Objects.requireNonNull(directLink, "Null directLink");
    }

    public static EntityLookupResult get(@Nonnull DictionaryLanguage language, @Nonnull EntityNode entityNode, @Nonnull SearchResultMatch matchResult, @Nonnull String directLink) {
        return new EntityLookupResult(language, entityNode, matchResult, directLink);
    }

    @Nonnull
    public OWLEntityData getOWLEntityData() {
        return getEntityNode().getEntityData();
    }

    @Nonnull
    public DictionaryLanguage getLanguage() {
        return language;
    }

    @Nonnull
    public EntityNode getEntityNode() {
        return entityNode;
    }

    @Nonnull
    public SearchResultMatch getMatchResult() {
        return matchResult;
    }

    @Nonnull
    public String getDirectLink() {
        return directLink;
    }
}
