package org.industrial.ontology.kernel.shortform;



import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;

import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.lang.ActiveLanguagesManager;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.lang.LanguageManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 6 Apr 2018
 */
public class LanguageManager {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final ActiveLanguagesManager activeLanguagesManager;

    @Nonnull
    private final ProjectDetailsRepository projectDetailsRepository;

    public LanguageManager(@Nonnull ProjectId projectId,
                           @Nonnull ActiveLanguagesManager extractor,
                           @Nonnull ProjectDetailsRepository projectDetailsRepository) {
        this.projectId = projectId;
        this.activeLanguagesManager = checkNotNull(extractor);
        this.projectDetailsRepository = projectDetailsRepository;
    }

    public synchronized List<DictionaryLanguage> getLanguages() {
        var defaultDisplayLanguages = projectDetailsRepository.getDisplayNameLanguages(projectId);
        if (defaultDisplayLanguages.isEmpty()) {
            return activeLanguagesManager.getLanguagesRankedByUsage();
        }
        else {
            return defaultDisplayLanguages;
        }
    }

    public synchronized List<DictionaryLanguage> getActiveLanguages() {
        return activeLanguagesManager.getLanguagesRankedByUsage();
    }
}
