package org.industrial.ontology.domain.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.domain.core.ProjectId;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.PrefixDeclarations}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22 Feb 2018
 */
public record PrefixDeclarations(@JsonProperty(PrefixDeclarations.PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(PrefixDeclarations.PREFIXES) @Nonnull ImmutableMap<String, String> prefixes) {

    public PrefixDeclarations {
        java.util.Objects.requireNonNull(projectId, "Null projectId");
        java.util.Objects.requireNonNull(prefixes, "Null prefixes");
    }

    public static final String PROJECT_ID = "_id";

    public static final String PREFIXES = "prefixes";

    /**
     * Gets an empty project prefixes for the specified project id.
     * @param projectId The project id.
     */
    public static PrefixDeclarations get(@Nonnull ProjectId projectId) {
        return new PrefixDeclarations(projectId, ImmutableMap.of());
    }

    /**
     * Creates a project prefixes object for the specified project and the specified prefixes.
     * @param projectId The projectId that identifies the project.
     * @param prefixes The prefixes.  A map of prefix names to prefixes.  Neither prefix names or prefixes are allowed
     *                 to be null.  Prefix names must end with colons.
     * @return The created {@link PrefixDeclarations}.
     */
    @JsonCreator
    public static PrefixDeclarations get(@JsonProperty(PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(PREFIXES) @Nonnull Map<String, String> prefixes) {
        checkNotNull(projectId);
        checkNotNull(prefixes);
        for (Map.Entry<String, String> entry : prefixes.entrySet()) {
            if (entry.getKey() == null) {
                throw new NullPointerException("Null prefix names are not allowed");
            }
            if (!entry.getKey().endsWith(":")) {
                throw new IllegalArgumentException("Prefix names must end with a colon");
            }
            if (entry.getValue() == null) {
                throw new NullPointerException("Prefixes must not be null.  " + "Prefix pointed to by " + entry.getKey() + " is null.");
            }
        }
        return new PrefixDeclarations(projectId, ImmutableMap.copyOf(prefixes));
    }

    @JsonIgnore
    @Nonnull
    public Optional<String> getPrefixForPrefixName(@Nonnull String prefixName) {
        checkArgument(prefixName.endsWith(":"), "Prefix names must end with a colon");
        return Optional.ofNullable(getPrefixes().get(prefixName));
    }

    @JsonProperty(PROJECT_ID)
    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    @JsonProperty(PREFIXES)
    @Nonnull
    public ImmutableMap<String, String> getPrefixes() {
        return prefixes;
    }
}
