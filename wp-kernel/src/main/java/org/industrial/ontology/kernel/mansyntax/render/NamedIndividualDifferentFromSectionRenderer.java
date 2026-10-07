package org.industrial.ontology.kernel.mansyntax.render;



import com.google.common.collect.Lists;
import org.industrial.ontology.kernel.api.index.DifferentIndividualsAxiomsIndex;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntax;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

import static java.util.stream.Collectors.toSet;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.NamedIndividualDifferentFromSectionRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class NamedIndividualDifferentFromSectionRenderer extends AbstractOWLAxiomItemSectionRenderer<OWLNamedIndividual, OWLDifferentIndividualsAxiom, OWLIndividual> {

    @Nonnull
    private final DifferentIndividualsAxiomsIndex axiomsIndex;

    public NamedIndividualDifferentFromSectionRenderer(@Nonnull DifferentIndividualsAxiomsIndex axiomsIndex) {
        this.axiomsIndex = checkNotNull(axiomsIndex);
    }

    @Override
    public ManchesterOWLSyntax getSection() {
        return ManchesterOWLSyntax.DIFFERENT_FROM;
    }

    @Override
    protected Set<OWLDifferentIndividualsAxiom> getAxiomsInOntology(OWLNamedIndividual subject, OWLOntologyID ontologyId) {
        return axiomsIndex.getDifferentIndividualsAxioms(subject, ontologyId).collect(toSet());
    }

    @Override
    public List<OWLIndividual> getRenderablesForItem(OWLNamedIndividual subject,
                                                     OWLDifferentIndividualsAxiom item,
                                                     OWLOntologyID ontologyId) {
        Set<OWLIndividual> setResult = item.getIndividuals();
        setResult.remove(subject);
        return Lists.newArrayList(setResult);
    }
}
