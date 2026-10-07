package org.industrial.ontology.kernel.mansyntax;

import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.frame.HasFreshEntities;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.change.ReverseEngineeredChangeDescriptionGeneratorFactory;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ManchesterSyntaxChangeGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.ManchesterSyntaxChangeGeneratorFactory} (generated in the legacy build).
 */
public final class ManchesterSyntaxChangeGeneratorFactory {

    private final Supplier<ManchesterSyntaxFrameParser> parser;

    private final Supplier<OntologyAxiomPairChangeGenerator> changeGenerator;

    private final Supplier<ReverseEngineeredChangeDescriptionGeneratorFactory> factory;

    public ManchesterSyntaxChangeGeneratorFactory(Supplier<ManchesterSyntaxFrameParser> parser,
            Supplier<OntologyAxiomPairChangeGenerator> changeGenerator,
            Supplier<ReverseEngineeredChangeDescriptionGeneratorFactory> factory) {
        this.parser = java.util.Objects.requireNonNull(parser);
        this.changeGenerator = java.util.Objects.requireNonNull(changeGenerator);
        this.factory = java.util.Objects.requireNonNull(factory);
    }

    public ManchesterSyntaxChangeGenerator create(@Nonnull OWLEntityData subject, @Nonnull String from, @Nonnull String to, @Nonnull String commitMessage, @Nonnull HasFreshEntities hasFreshEntities) {
        return new ManchesterSyntaxChangeGenerator(parser.get(), changeGenerator.get(), factory.get(), subject, from, to, commitMessage, hasFreshEntities);
    }
}
