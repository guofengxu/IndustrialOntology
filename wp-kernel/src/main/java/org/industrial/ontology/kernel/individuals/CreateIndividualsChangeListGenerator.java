package org.industrial.ontology.kernel.individuals;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.change.AbstractCreateEntitiesChangeListGenerator;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.util.ClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.model.EntityType.NAMED_INDIVIDUAL;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.individuals.CreateIndividualsChangeListGenerator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 12/09/2013
 */
public class CreateIndividualsChangeListGenerator extends AbstractCreateEntitiesChangeListGenerator<OWLNamedIndividual, OWLClass> {

    @Nonnull
    private final OWLDataFactory dataFactory;

    public CreateIndividualsChangeListGenerator(@Nonnull OWLDataFactory dataFactory,
                                                @Nonnull MessageFormatter msg,
                                                @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                                                @Nonnull ImmutableSet<OWLClass> parents,
                                                @Nonnull String sourceText,
                                                @Nonnull String langTag) {
        super(NAMED_INDIVIDUAL, sourceText, langTag, parents, dataFactory, msg, defaultOntologyIdManager);
        this.dataFactory = checkNotNull(dataFactory);
    }

    @Override
    protected Set<? extends OWLAxiom> createParentPlacementAxioms(OWLNamedIndividual freshEntity,
                                                                  ChangeGenerationContext context,
                                                                  ImmutableSet<OWLClass> parents) {
        return parents.stream()
                .filter(ClassExpression::isNotOwlThing)
                .map(parent -> dataFactory.getOWLClassAssertionAxiom(parent, freshEntity))
                .collect(toSet());
    }
}
