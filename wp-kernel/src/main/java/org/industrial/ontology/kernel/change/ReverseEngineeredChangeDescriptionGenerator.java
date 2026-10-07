package org.industrial.ontology.kernel.change;



import org.industrial.ontology.kernel.change.matcher.ChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.ChangeSummary;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ReverseEngineeredChangeDescriptionGenerator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class ReverseEngineeredChangeDescriptionGenerator<S> implements ChangeDescriptionGenerator<S> {

    @Nonnull
    private final String defaultDescription;

    @Nonnull
    private final List<ChangeMatcher> matchers;

    @Nonnull
    private final OWLObjectStringFormatter formatter;

    public ReverseEngineeredChangeDescriptionGenerator(@Nonnull String defaultDescription,
                                                       @Nonnull Set<ChangeMatcher> matchers,
                                                       @Nonnull OWLObjectStringFormatter formatter) {
        this.defaultDescription = checkNotNull(defaultDescription);
        this.matchers = new ArrayList<>(matchers);
        this.formatter = checkNotNull(formatter);
    }

    @Override
    public String generateChangeDescription(ChangeApplicationResult<S> result) {
        var changes = result.getChangeList();
        for(ChangeMatcher matcher : matchers) {
            Optional<ChangeSummary> description = matcher.getDescription(changes);
            if(description.isPresent()) {
                return description.get().getDescription().formatDescription(formatter);
            }
        }
        return defaultDescription;
    }
}
