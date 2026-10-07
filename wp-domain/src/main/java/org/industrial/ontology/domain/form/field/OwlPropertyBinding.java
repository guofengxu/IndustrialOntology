package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.match.CompositeRelationshipValueCriteria;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.OwlPropertyBinding}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-24
 */
@JsonTypeName(OwlPropertyBinding.TYPE)
public record OwlPropertyBinding(@Nonnull OWLProperty property, @JsonProperty(VALUES_CRITERIA) @Nullable CompositeRelationshipValueCriteria valuesCriteriaInternal) implements OwlBinding {

    public OwlPropertyBinding {
        Objects.requireNonNull(property, "Null property");
    }

    public static final String TYPE = "PROPERTY";

    public static final String PROPERTY = "property";

    @JsonCreator
    public static OwlPropertyBinding get(@JsonProperty(PROPERTY) @Nonnull OWLProperty property, @JsonProperty(VALUES_CRITERIA) @Nullable CompositeRelationshipValueCriteria criteria) {
        return new OwlPropertyBinding(property, criteria);
    }

    public static OwlPropertyBinding get(@JsonProperty(PROPERTY) @Nonnull OWLProperty property) {
        return get(property, null);
    }

    @Nonnull
    @JsonIgnore
    @Override
    public Optional<OWLProperty> getOwlProperty() {
        return Optional.of(getProperty());
    }

    @JsonIgnore
    @Nonnull
    public Optional<CompositeRelationshipValueCriteria> getValuesCriteria() {
        return Optional.ofNullable(getValuesCriteriaInternal());
    }

    @Nonnull
    public OWLProperty getProperty() {
        return property;
    }

    @JsonProperty(VALUES_CRITERIA)
    @Nullable
    public CompositeRelationshipValueCriteria getValuesCriteriaInternal() {
        return valuesCriteriaInternal;
    }
}
