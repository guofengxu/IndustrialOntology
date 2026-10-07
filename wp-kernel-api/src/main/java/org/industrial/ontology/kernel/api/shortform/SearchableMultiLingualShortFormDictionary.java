package org.industrial.ontology.kernel.api.shortform;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.SearchableMultiLingualShortFormDictionary}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-02
 *
 * Represents an interface for searching multi-lingual short forms
 */
public interface SearchableMultiLingualShortFormDictionary {
    /**
     * Gets short forms containing the specified search strings
     *
     * @param searchStrings The search strings.
     * @param entityTypes   The types of entities to be retrieved.
     * @param languages     The list of languages to consider.
     * @param searchFilters A list of search filters.  An empty list indicates no filtering.
     * @param pageRequest   A page request for the short forms
     * @return A page of short forms.
     */
    @Nonnull
    Page<EntityShortFormMatches> getShortFormsContaining(@Nonnull List<SearchString> searchStrings,
                                                         @Nonnull Set<EntityType<?>> entityTypes,
                                                         @Nonnull List<DictionaryLanguage> languages,
                                                         @Nonnull ImmutableList<EntitySearchFilter> searchFilters,
                                                         @Nonnull PageRequest pageRequest);
}
