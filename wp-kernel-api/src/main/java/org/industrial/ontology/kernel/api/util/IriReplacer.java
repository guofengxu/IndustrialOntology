package org.industrial.ontology.kernel.api.util;



import com.google.common.collect.ImmutableMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.IriReplacer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-28
 *
 * Replaces IRIs in an OWLObject.  Note IRI replacers are not thread safe.
 */
public class IriReplacer {

    @Nonnull
    private final OWLObjectDuplicator duplicator;

    public IriReplacer(@Nonnull OWLDataFactory dataFactory,
                       @Nonnull ImmutableMap<IRI, IRI> iriMap) {
        this.duplicator = new OWLObjectDuplicator(dataFactory, checkNotNull(iriMap));
    }


    public <O extends OWLObject> O replaceIris(O owlObject) {
        return duplicator.duplicateObject(owlObject);
    }
}
