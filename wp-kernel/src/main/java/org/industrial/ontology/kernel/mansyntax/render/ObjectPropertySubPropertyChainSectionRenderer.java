package org.industrial.ontology.kernel.mansyntax.render;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import javax.annotation.Nonnull;

import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.ObjectPropertySubPropertyChainSectionRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ObjectPropertySubPropertyChainSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLObjectProperty, OWLSubPropertyChainOfAxiom, OWLObjectPropertyExpression> {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public ObjectPropertySubPropertyChainSectionRenderer(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.SUB_PROPERTY_CHAIN;
    }

    @Override
    public Set<OWLSubPropertyChainOfAxiom> getAxiomsInOntology(OWLObjectProperty subject,
                                                               OWLOntologyID ontologyId) {
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_PROPERTY_CHAIN_OF, ontologyId)
                         .filter(ax -> ax.getSuperProperty().equals(subject))
                         .collect(toSet());
    }

    @Override
    public List<OWLObjectPropertyExpression> getRenderablesForItem(OWLObjectProperty subject,
                                                                   OWLSubPropertyChainOfAxiom item,
                                                                   OWLOntologyID ontologyId) {
        return item.getPropertyChain();
    }

    @Override
    public String getSeparatorAfter(int renderableIndex, List<OWLObjectPropertyExpression> renderables) {
        return "<span class=\"ms-connective-kw\"> o </span>";
    }
}
