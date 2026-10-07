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
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.OwlInstanceBinding}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-03-26
 */
@JsonTypeName(OwlInstanceBinding.TYPE)
public record OwlInstanceBinding() implements OwlBinding {

    protected static final String TYPE = "INSTANCE";

    @JsonCreator
    @Nonnull
    public static OwlInstanceBinding get(@JsonProperty(VALUES_CRITERIA) @Nullable EntityMatchCriteria valuesFilter) {
        return new OwlInstanceBinding();
    }

    @Nonnull
    public static OwlInstanceBinding get() {
        return get(null);
    }

    @Nonnull
    @Override
    public Optional<OWLProperty> getOwlProperty() {
        return Optional.empty();
    }
}
