package org.industrial.ontology.kernel.diff;

import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link Revision2DiffElementsTranslator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.diff.Revision2DiffElementsTranslatorFactory} (generated in the legacy build).
 */
public final class Revision2DiffElementsTranslatorFactory {

    private final Supplier<OntologyIRIShortFormProvider> ontologyIRIShortFormProvider;

    private final Supplier<DefaultOntologyIdManager> defaultOntologyIdManager;

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    public Revision2DiffElementsTranslatorFactory(Supplier<OntologyIRIShortFormProvider> ontologyIRIShortFormProvider,
            Supplier<DefaultOntologyIdManager> defaultOntologyIdManager,
            Supplier<ProjectOntologiesIndex> projectOntologiesIndex) {
        this.ontologyIRIShortFormProvider = java.util.Objects.requireNonNull(ontologyIRIShortFormProvider);
        this.defaultOntologyIdManager = java.util.Objects.requireNonNull(defaultOntologyIdManager);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
    }

    public Revision2DiffElementsTranslator create() {
        return new Revision2DiffElementsTranslator(ontologyIRIShortFormProvider.get(), defaultOntologyIdManager.get(), projectOntologiesIndex.get());
    }
}
