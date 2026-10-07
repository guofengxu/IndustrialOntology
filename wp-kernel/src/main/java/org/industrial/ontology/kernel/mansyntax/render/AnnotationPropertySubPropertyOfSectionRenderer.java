package org.industrial.ontology.kernel.mansyntax.render;



import com.google.common.collect.Lists;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

import static java.util.stream.Collectors.toSet;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.AnnotationPropertySubPropertyOfSectionRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class AnnotationPropertySubPropertyOfSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLAnnotationProperty, OWLSubAnnotationPropertyOfAxiom, OWLAnnotationProperty> {

    public AnnotationPropertySubPropertyOfSectionRenderer(@Nonnull SubAnnotationPropertyAxiomsBySubPropertyIndex axiomsIndex) {
        this.axiomsIndex = checkNotNull(axiomsIndex);
    }

    @Nonnull
    private final SubAnnotationPropertyAxiomsBySubPropertyIndex axiomsIndex;

    @Override
    protected Set<OWLSubAnnotationPropertyOfAxiom> getAxiomsInOntology(OWLAnnotationProperty subject,
                                                                       OWLOntologyID ontologyId) {
        return axiomsIndex.getSubPropertyOfAxioms(subject, ontologyId).collect(toSet());
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.SUB_PROPERTY_OF;
    }

    @Override
    public List<OWLAnnotationProperty> getRenderablesForItem(OWLAnnotationProperty subject,
                                                             OWLSubAnnotationPropertyOfAxiom item,
                                                             OWLOntologyID ontologyId) {
        return Lists.newArrayList(item.getSuperProperty());
    }
}
