package org.industrial.ontology.kernel.mansyntax;



import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.shortform.WebProtegeOntologyIRIShortFormProvider;
import org.semanticweb.owlapi.expression.OWLOntologyChecker;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.ShellOntologyChecker}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public class ShellOntologyChecker implements OWLOntologyChecker {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final WebProtegeOntologyIRIShortFormProvider shortFormProvider;

    private final DefaultOntologyIdManager defaultOntologyIdManager;

    public ShellOntologyChecker(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                @Nonnull WebProtegeOntologyIRIShortFormProvider shortFormProvider,
                                DefaultOntologyIdManager defaultOntologyIdManager) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.shortFormProvider = checkNotNull(shortFormProvider);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
    }

    @Nullable
    @Override
    public OWLOntology getOntology(@Nullable String name) {
        if(name == null) {
            OWLOntologyID defaultOntologyId = defaultOntologyIdManager.getDefaultOntologyId();
            return ShellOwlOntology.get(defaultOntologyId);
        }
        return projectOntologiesIndex.getOntologyIds()
                                     .filter(ontId -> shortFormProvider.getShortForm(ontId)
                                                                       .equalsIgnoreCase(name))
                                     .findFirst()
                                     .map(ShellOwlOntology::new)
                                     .orElse(null);
    }
}
