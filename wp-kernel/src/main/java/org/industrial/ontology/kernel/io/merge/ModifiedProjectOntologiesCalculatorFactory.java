package org.industrial.ontology.kernel.io.merge;

import org.industrial.ontology.kernel.project.Ontology;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ModifiedProjectOntologiesCalculator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.ModifiedProjectOntologiesCalculatorFactory} (generated in the legacy build).
 */
public final class ModifiedProjectOntologiesCalculatorFactory {

    private final Supplier<OntologyDiffCalculator> diffCalculator;

    public ModifiedProjectOntologiesCalculatorFactory(Supplier<OntologyDiffCalculator> diffCalculator) {
        this.diffCalculator = java.util.Objects.requireNonNull(diffCalculator);
    }

    public ModifiedProjectOntologiesCalculator create(@Nonnull Collection<Ontology> projectOntologies, @Nonnull Collection<Ontology> editedOntologies) {
        return new ModifiedProjectOntologiesCalculator(projectOntologies, editedOntologies, diffCalculator.get());
    }
}
