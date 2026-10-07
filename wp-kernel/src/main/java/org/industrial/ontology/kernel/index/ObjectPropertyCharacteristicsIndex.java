package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ObjectPropertyCharacteristicsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public class ObjectPropertyCharacteristicsIndex implements org.industrial.ontology.kernel.api.index.ObjectPropertyCharacteristicsIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public ObjectPropertyCharacteristicsIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Override
    public boolean hasCharacteristic(@Nonnull OWLObjectProperty property,
                                     @Nonnull ObjectPropertyCharacteristic characteristic,
                                     @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(property);
        checkNotNull(characteristic);
        checkNotNull(ontologyId);
        var axiomType = getAxiomType(characteristic);
        return axiomsByTypeIndex.getAxiomsByType(axiomType, ontologyId)
                                .anyMatch(ax -> ax.getProperty()
                                                  .equals(property));
    }

    private AxiomType<? extends OWLObjectPropertyCharacteristicAxiom> getAxiomType(ObjectPropertyCharacteristic characteristic) {
        switch(characteristic) {
            case FUNCTIONAL:
                return AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
            case INVERSE_FUNCTIONAL:
                return AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
            case SYMMETRIC:
                return AxiomType.SYMMETRIC_OBJECT_PROPERTY;
            case ASYMMETRIC:
                return AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
            case REFLEXIVE:
                return AxiomType.REFLEXIVE_OBJECT_PROPERTY;
            case IRREFLEXIVE:
                return AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
            case TRANSITIVE:
                return AxiomType.TRANSITIVE_OBJECT_PROPERTY;
            default:
                throw new RuntimeException("Unknown characteristic: " + characteristic);
        }
    }
}
