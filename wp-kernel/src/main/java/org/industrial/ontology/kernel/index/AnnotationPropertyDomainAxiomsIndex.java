package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationPropertyDomainAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-13
 */
public class AnnotationPropertyDomainAxiomsIndex implements org.industrial.ontology.kernel.api.index.AnnotationPropertyDomainAxiomsIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public AnnotationPropertyDomainAxiomsIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        // Perform an index scan on property domain axioms (there's likely to be few)
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(@Nonnull OWLAnnotationProperty property,
                                                                                      @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(property);
        checkNotNull(ontologyId);
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.ANNOTATION_PROPERTY_DOMAIN, ontologyId)
                                .filter(ax -> ax.getProperty()
                                                .equals(property));
    }
}
