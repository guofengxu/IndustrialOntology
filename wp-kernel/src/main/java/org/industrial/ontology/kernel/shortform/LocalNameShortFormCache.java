package org.industrial.ontology.kernel.shortform;



import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.shortform.LocalNameExtractor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LocalNameShortFormCache}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Apr 2018
 */
public class LocalNameShortFormCache {

    @Nonnull
    private final ShortFormCache shortFormCache;

    @Nonnull
    private final LocalNameExtractor localNameExtractor;

    public LocalNameShortFormCache(@Nonnull ShortFormCache shortFormCache,
                                   @Nonnull LocalNameExtractor localNameExtractor) {
        this.shortFormCache = checkNotNull(shortFormCache);
        this.localNameExtractor = checkNotNull(localNameExtractor);
    }

    public String getShortForm(OWLEntity entity) {
        String localNameShortForm = shortFormCache.getShortFormOrElse(entity, null);
        if(localNameShortForm == null) {
            String localName = localNameExtractor.getLocalName(entity.getIRI());
            if(localName.isEmpty()) {
                return entity.getIRI().toString();
            }
            else {
                shortFormCache.put(entity, localName);
                return localName;
            }
        }
        else {
            return localNameShortForm;
        }
    }
}
