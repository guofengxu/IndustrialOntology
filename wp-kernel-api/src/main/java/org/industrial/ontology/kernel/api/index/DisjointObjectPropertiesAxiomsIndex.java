package org.industrial.ontology.kernel.api.index;




import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.DisjointObjectPropertiesAxiomsIndex}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public interface DisjointObjectPropertiesAxiomsIndex extends Index {

    @Nonnull
    Stream<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(@Nonnull OWLObjectProperty property,
                                                                               @Nonnull OWLOntologyID ontologyId);
}
