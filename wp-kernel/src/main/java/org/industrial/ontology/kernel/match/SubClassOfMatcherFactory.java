package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.domain.match.HierarchyFilterType;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link SubClassOfMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.SubClassOfMatcherFactory} (generated in the legacy build).
 */
public final class SubClassOfMatcherFactory {

    private final Supplier<ClassHierarchyProvider> provider;

    public SubClassOfMatcherFactory(Supplier<ClassHierarchyProvider> provider) {
        this.provider = java.util.Objects.requireNonNull(provider);
    }

    public SubClassOfMatcher create(@Nonnull OWLClass cls, @Nonnull HierarchyFilterType filterType) {
        return new SubClassOfMatcher(provider.get(), cls, filterType);
    }
}
