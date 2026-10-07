package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.OwlClassBinding}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-25
 */
@JsonTypeName(OwlClassBinding.TYPE)
public record OwlClassBinding() implements OwlBinding {

    public static final String TYPE = "CLASS";

    @JsonCreator
    @Nonnull
    public static OwlClassBinding get(@JsonProperty(VALUES_CRITERIA) @Nullable EntityMatchCriteria valuesFilter) {
        return new OwlClassBinding();
    }

    @Nonnull
    public static OwlClassBinding get() {
        return get(null);
    }

    @Nonnull
    @Override
    public Optional<OWLProperty> getOwlProperty() {
        return Optional.empty();
    }
}
