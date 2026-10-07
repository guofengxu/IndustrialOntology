package org.industrial.ontology.kernel.shortform;



import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.IRIShortFormProvider;

import javax.annotation.Nonnull;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.IriShortFormAdapter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-21
 */
public class IriShortFormAdapter implements IRIShortFormProvider {

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitesByIri;

    @Nonnull
    private final DictionaryManager dictionaryManager;

    public IriShortFormAdapter(@Nonnull EntitiesInProjectSignatureByIriIndex entitesByIri,
                               @Nonnull DictionaryManager dictionaryManager) {
        this.entitesByIri = entitesByIri;
        this.dictionaryManager = dictionaryManager;
    }

    @Nonnull
    @Override
    public String getShortForm(@Nonnull IRI iri) {
        return entitesByIri.getEntitiesInSignature(iri)
                .sorted()
                .map(dictionaryManager::getShortForm)
                .findFirst()
                .orElse(iri.toQuotedString());
    }
}
