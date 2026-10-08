package org.industrial.ontology.kernel.io.merge;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.merge.Diff;
import org.industrial.ontology.kernel.project.Ontology;
import org.semanticweb.owlapi.model.OWLAxiom;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.AxiomDiffCalculator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/01/15
 */
public class AxiomDiffCalculator {

    public AxiomDiffCalculator() {
    }

    public Diff<OWLAxiom> computeDiff(@Nonnull Ontology from,
                                      @Nonnull Ontology to) {
        var fromAxioms = checkNotNull(from).getAxioms();
        var toAxioms = checkNotNull(to).getAxioms();
        var addedAxioms = ImmutableSet.<OWLAxiom>builder();
        var removedAxioms = ImmutableSet.<OWLAxiom>builder();
        for(var toAx : to.getAxioms()) {
            if(!fromAxioms.contains(toAx)) {
                addedAxioms.add(toAx);
            }
        }
        for(var fromAx : from.getAxioms()) {
            if(!toAxioms.contains(fromAx)) {
                removedAxioms.add(fromAx);
            }
        }
        return new Diff<>(addedAxioms.build(), removedAxioms.build());
    }
}
