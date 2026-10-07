package org.industrial.ontology.kernel.api.shortform;



import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.MultilingualDictionaryUpdater}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-01
 */
public interface MultilingualDictionaryUpdater {

    /**
     * Updates the dictionary entries for the specified entities.
     * @param entities The entities that will be updated.
     * @param languages The languages that should be updated.
     */
    void update(@Nonnull Collection<OWLEntity> entities,
                @Nonnull List<DictionaryLanguage> languages);
}
