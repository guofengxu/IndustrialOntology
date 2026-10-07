package org.industrial.ontology.kernel.change;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.util.ClassExpression;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;
import static org.semanticweb.owlapi.model.EntityType.CLASS;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.CreateClassesChangeGenerator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 */
public class CreateClassesChangeGenerator extends AbstractCreateEntitiesChangeListGenerator<OWLClass, OWLClass> {

    @Nonnull
    private final OWLDataFactory dataFactory;

    public CreateClassesChangeGenerator(@Nonnull OWLDataFactory dataFactory,
                                        @Nonnull MessageFormatter msg,
                                        @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                                        @Nonnull String sourceText,
                                        @Nonnull String langTag,
                                        @Nonnull ImmutableSet<OWLClass> parent) {
        super(CLASS, sourceText, langTag, parent, dataFactory, msg, defaultOntologyIdManager);
        this.dataFactory = checkNotNull(dataFactory);
    }

    @Override
    protected Set<? extends OWLAxiom> createParentPlacementAxioms(OWLClass freshEntity,
                                                                  ChangeGenerationContext context,
                                                                  ImmutableSet<OWLClass> parents) {
        return parents.stream()
                .filter(ClassExpression::isNotOwlThing)
                .map(parent -> dataFactory.getOWLSubClassOfAxiom(freshEntity, parent))
                .collect(toSet());
    }
}
