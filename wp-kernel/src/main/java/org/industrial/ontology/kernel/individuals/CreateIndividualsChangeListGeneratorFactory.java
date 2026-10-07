package org.industrial.ontology.kernel.individuals;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link CreateIndividualsChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.individuals.CreateIndividualsChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class CreateIndividualsChangeListGeneratorFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<MessageFormatter> msg;

    private final Supplier<DefaultOntologyIdManager> defaultOntologyIdManager;

    public CreateIndividualsChangeListGeneratorFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<MessageFormatter> msg,
            Supplier<DefaultOntologyIdManager> defaultOntologyIdManager) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.msg = java.util.Objects.requireNonNull(msg);
        this.defaultOntologyIdManager = java.util.Objects.requireNonNull(defaultOntologyIdManager);
    }

    public CreateIndividualsChangeListGenerator create(@Nonnull ImmutableSet<OWLClass> parents, @Nonnull String sourceText, @Nonnull String langTag) {
        return new CreateIndividualsChangeListGenerator(dataFactory.get(), msg.get(), defaultOntologyIdManager.get(), parents, sourceText, langTag);
    }
}
