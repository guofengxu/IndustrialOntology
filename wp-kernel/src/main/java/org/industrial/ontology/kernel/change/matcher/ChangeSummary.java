package org.industrial.ontology.kernel.change.matcher;

import org.industrial.ontology.kernel.change.description.StructuredChangeDescription;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.ChangeSummary}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record ChangeSummary(@Nonnull StructuredChangeDescription description) {

    public ChangeSummary {
        Objects.requireNonNull(description, "Null description");
    }

    public static ChangeSummary get(@Nonnull StructuredChangeDescription description) {
        return new ChangeSummary(description);
    }

    @Nonnull
    public StructuredChangeDescription getDescription() {
        return description;
    }
}
