package org.industrial.ontology.kernel.mansyntax.render;



import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.ClassSubClassOfSectionRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ClassSubClassOfSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLClass, OWLSubClassOfAxiom, OWLClassExpression> {

    @Nonnull
    private final SubClassOfAxiomsBySubClassIndex axiomsIndex;

    public ClassSubClassOfSectionRenderer(@Nonnull SubClassOfAxiomsBySubClassIndex axiomsIndex) {
        this.axiomsIndex = checkNotNull(axiomsIndex);
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.SUBCLASS_OF;
    }

    @Override
    protected Set<OWLSubClassOfAxiom> getAxiomsInOntology(OWLClass subject, OWLOntologyID ontologyId) {
        return axiomsIndex.getSubClassOfAxiomsForSubClass(subject, ontologyId).collect(toSet());
    }

    @Override
    public List<OWLClassExpression> getRenderablesForItem(OWLClass subject,
                                                          OWLSubClassOfAxiom item,
                                                          OWLOntologyID ontologyId) {
        return singletonList(item.getSuperClass());
    }

    @Override
    public String getSeparatorAfter(int renderableIndex, List<OWLClassExpression> renderables) {
        return "";
    }
}
