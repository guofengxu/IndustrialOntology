package org.industrial.ontology.kernel.change;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link CreateObjectPropertiesChangeGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.CreateObjectPropertiesChangeGeneratorFactory} (generated in the legacy build).
 */
public final class CreateObjectPropertiesChangeGeneratorFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<MessageFormatter> msg;

    private final Supplier<DefaultOntologyIdManager> defaultOntologyIdManager;

    public CreateObjectPropertiesChangeGeneratorFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<MessageFormatter> msg,
            Supplier<DefaultOntologyIdManager> defaultOntologyIdManager) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.msg = java.util.Objects.requireNonNull(msg);
        this.defaultOntologyIdManager = java.util.Objects.requireNonNull(defaultOntologyIdManager);
    }

    public CreateObjectPropertiesChangeGenerator create(@Nonnull String sourceText, @Nonnull String langTag, @Nonnull ImmutableSet<OWLObjectProperty> parents) {
        return new CreateObjectPropertiesChangeGenerator(dataFactory.get(), msg.get(), defaultOntologyIdManager.get(), sourceText, langTag, parents);
    }
}
