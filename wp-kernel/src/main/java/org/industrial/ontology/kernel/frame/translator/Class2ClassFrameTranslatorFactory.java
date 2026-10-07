package org.industrial.ontology.kernel.frame.translator;

import org.industrial.ontology.kernel.frame.PropertyValueMinimiser;
import org.industrial.ontology.kernel.api.hierarchy.HasGetAncestors;
import org.industrial.ontology.kernel.api.match.RelationshipMatcherFactory;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex;
import org.semanticweb.owlapi.model.OWLClass;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link Class2ClassFrameTranslator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.Class2ClassFrameTranslatorFactory} (generated in the legacy build).
 */
public final class Class2ClassFrameTranslatorFactory {

    private final Supplier<ClassFrameAxiomsIndex> classFrameAxiomsIndex;

    private final Supplier<HasGetAncestors<OWLClass>> ancestorsProvider;

    private final Supplier<PropertyValueMinimiser> propertyValueMinimiser;

    private final Supplier<AxiomPropertyValueTranslator> axiomPropertyValueTranslator;

    private final Supplier<RelationshipMatcherFactory> matcherFactory;

    public Class2ClassFrameTranslatorFactory(Supplier<ClassFrameAxiomsIndex> classFrameAxiomsIndex,
            Supplier<HasGetAncestors<OWLClass>> ancestorsProvider,
            Supplier<PropertyValueMinimiser> propertyValueMinimiser,
            Supplier<AxiomPropertyValueTranslator> axiomPropertyValueTranslator,
            Supplier<RelationshipMatcherFactory> matcherFactory) {
        this.classFrameAxiomsIndex = java.util.Objects.requireNonNull(classFrameAxiomsIndex);
        this.ancestorsProvider = java.util.Objects.requireNonNull(ancestorsProvider);
        this.propertyValueMinimiser = java.util.Objects.requireNonNull(propertyValueMinimiser);
        this.axiomPropertyValueTranslator = java.util.Objects.requireNonNull(axiomPropertyValueTranslator);
        this.matcherFactory = java.util.Objects.requireNonNull(matcherFactory);
    }

    public Class2ClassFrameTranslator create(@Nonnull ClassFrameTranslationOptions options) {
        return new Class2ClassFrameTranslator(classFrameAxiomsIndex.get(), ancestorsProvider.get(), propertyValueMinimiser.get(), axiomPropertyValueTranslator.get(), matcherFactory.get(), options);
    }
}
