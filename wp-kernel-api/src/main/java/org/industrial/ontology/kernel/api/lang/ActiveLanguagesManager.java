package org.industrial.ontology.kernel.api.lang;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.domain.lang.DictionaryLanguageUsage;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;

import java.util.List;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.lang.ActiveLanguagesManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-05
 */
public interface ActiveLanguagesManager {

    /**
     * Gets the languages used in the project, ranked in descending order in terms
     * of usage – the most commonly used languages appear first.  This ranking may change
     * as the ontologies in a project are edited.
     */
    @Nonnull
    ImmutableList<DictionaryLanguage> getLanguagesRankedByUsage();

    /**
     * Gets the languages used in the project, ranked in descending order in terms of usage.  The most commonly
     * user languages appear first.
     */
    @Nonnull
    ImmutableList<DictionaryLanguageUsage> getLanguageUsage();

    /**
     * Updates the active languages from the list of applied changes
     *
     * @param changes The changes.
     */
    void handleChanges(@Nonnull List<OntologyChange> changes);
}
