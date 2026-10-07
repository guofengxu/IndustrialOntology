package org.industrial.ontology.kernel.mansyntax.render;



import com.google.common.collect.Lists;
import org.industrial.ontology.kernel.api.index.EquivalentObjectPropertiesAxiomsIndex;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

import static java.util.stream.Collectors.toSet;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.ObjectPropertyEquivalentToSectionRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ObjectPropertyEquivalentToSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLObjectProperty, OWLEquivalentObjectPropertiesAxiom, OWLObjectPropertyExpression> {

    @Nonnull
    private final EquivalentObjectPropertiesAxiomsIndex axiomsIndex;

    public ObjectPropertyEquivalentToSectionRenderer(@Nonnull EquivalentObjectPropertiesAxiomsIndex axiomsIndex) {
        this.axiomsIndex = checkNotNull(axiomsIndex);
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.EQUIVALENT_TO;
    }

    @Override
    protected Set<OWLEquivalentObjectPropertiesAxiom> getAxiomsInOntology(OWLObjectProperty subject,
                                                                          OWLOntologyID ontologyId) {
        return axiomsIndex.getEquivalentObjectPropertiesAxioms(subject, ontologyId).collect(toSet());
    }

    @Override
    public List<OWLObjectPropertyExpression> getRenderablesForItem(OWLObjectProperty subject,
                                                                   OWLEquivalentObjectPropertiesAxiom item,
                                                                   OWLOntologyID ontologyId) {
        return Lists.newArrayList(item.getPropertiesMinus(subject));
    }

    @Override
    public String getSeparatorAfter(int renderableIndex, List<OWLObjectPropertyExpression> renderables) {
        return ", ";
    }
}
