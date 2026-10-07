package org.industrial.ontology.domain.entity;



import com.google.common.base.Splitter;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityShortFormsParser}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 7 Dec 2017
 */
public class EntityShortFormsParser {

    public EntityShortFormsParser() {
    }

    @Nonnull
    public List<String> parseShortForms(@Nonnull String sourceText) {
        return Splitter.on("\n")
                                     .omitEmptyStrings()
                                     .trimResults()
                                     .splitToList(sourceText);
    }
}
