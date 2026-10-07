package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubDataPropertyAxiomsBySubPropertyIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class SubDataPropertyAxiomsBySubPropertyIndex implements org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public SubDataPropertyAxiomsBySubPropertyIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLSubDataPropertyOfAxiom> getSubPropertyOfAxioms(@Nonnull OWLDataProperty dataProperty,
                                                                    @Nonnull OWLOntologyID ontologyID) {
        checkNotNull(ontologyID);
        checkNotNull(dataProperty);
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_DATA_PROPERTY, ontologyID)
                                .filter(ax -> ax.getSubProperty()
                                                .equals(dataProperty));
    }
}
