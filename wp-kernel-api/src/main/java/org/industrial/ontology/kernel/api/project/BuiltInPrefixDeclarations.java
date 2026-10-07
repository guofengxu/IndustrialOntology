package org.industrial.ontology.kernel.api.project;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.project.PrefixDeclaration;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.BuiltInPrefixDeclarations}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-04-30
 */
public record BuiltInPrefixDeclarations(@Nonnull ImmutableList<PrefixDeclaration> prefixDeclarations) {

    public BuiltInPrefixDeclarations {
        Objects.requireNonNull(prefixDeclarations, "Null prefixDeclarations");
    }

    @Nonnull
    public static BuiltInPrefixDeclarations get(@Nonnull ImmutableList<PrefixDeclaration> prefixDeclarations) {
        return new BuiltInPrefixDeclarations(prefixDeclarations);
    }

    /**
     * Gets a list of prefix declarations that are considered to be built in prefix
     * declarations.
     */
    @Nonnull
    public ImmutableList<PrefixDeclaration> getPrefixDeclarations() {
        return prefixDeclarations;
    }
}
