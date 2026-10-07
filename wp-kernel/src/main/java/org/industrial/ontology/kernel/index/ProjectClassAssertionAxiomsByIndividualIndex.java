package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;

import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ProjectClassAssertionAxiomsByIndividualIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class ProjectClassAssertionAxiomsByIndividualIndex implements org.industrial.ontology.kernel.api.index.ProjectClassAssertionAxiomsByIndividualIndex, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final ClassAssertionAxiomsByIndividualIndex classAssertionAxiomsByIndividualIndex;

    public ProjectClassAssertionAxiomsByIndividualIndex(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                                            @Nonnull ClassAssertionAxiomsByIndividualIndex classAssertionAxiomsByIndividualIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.classAssertionAxiomsByIndividualIndex = checkNotNull(classAssertionAxiomsByIndividualIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(projectOntologiesIndex, classAssertionAxiomsByIndividualIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull OWLIndividual individual) {
        checkNotNull(individual);
        return projectOntologiesIndex.getOntologyIds()
                .flatMap(ontId -> classAssertionAxiomsByIndividualIndex.getClassAssertionAxioms(individual, ontId));
    }
}
