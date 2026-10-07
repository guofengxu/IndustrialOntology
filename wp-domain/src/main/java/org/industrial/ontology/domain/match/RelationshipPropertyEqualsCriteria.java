package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipPropertyEqualsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeName("PropertyEquals")
public record RelationshipPropertyEqualsCriteria(@JsonProperty(RelationshipPropertyEqualsCriteria.PROPERTY) @Nonnull OWLProperty property) implements RelationshipPropertyCriteria {

    public RelationshipPropertyEqualsCriteria {
        Objects.requireNonNull(property, "Null property");
    }

    private static final String PROPERTY = "property";

    @JsonCreator
    @Nonnull
    public static RelationshipPropertyEqualsCriteria get(@Nonnull @JsonProperty(PROPERTY) OWLProperty property) {
        return new RelationshipPropertyEqualsCriteria(property);
    }

    @Override
    public <R> R accept(@Nonnull RelationshipPropertyCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(PROPERTY)
    @Nonnull
    public OWLProperty getProperty() {
        return property;
    }
}
