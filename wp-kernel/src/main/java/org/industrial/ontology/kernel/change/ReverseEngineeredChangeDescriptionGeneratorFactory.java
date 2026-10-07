package org.industrial.ontology.kernel.change;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.change.matcher.ChangeMatcher;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import java.util.Set;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ReverseEngineeredChangeDescriptionGeneratorFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class ReverseEngineeredChangeDescriptionGeneratorFactory {

    @Nonnull
    private Set<ChangeMatcher> changeMatchers;

    @Nonnull
    private OWLObjectStringFormatter formatter;

    public ReverseEngineeredChangeDescriptionGeneratorFactory(@Nonnull Set<ChangeMatcher> changeMatchers,
                                                              @Nonnull OWLObjectStringFormatter formatter) {
        this.changeMatchers = ImmutableSet.copyOf(changeMatchers);
        this.formatter = formatter;
    }

    public <S extends OWLEntity> ReverseEngineeredChangeDescriptionGenerator<S> get(String defaultDescription) {
        return new ReverseEngineeredChangeDescriptionGenerator<>(defaultDescription, changeMatchers, formatter);
    }
}
