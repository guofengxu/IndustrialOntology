package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.SubClassOfCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 17 Jun 2018
 */
@JsonTypeName("SubClassOf")
public record SubClassOfCriteria(@JsonProperty(SubClassOfCriteria.TARGET) @Nonnull OWLClass target, @JsonProperty(SubClassOfCriteria.FILTER_TYPE) HierarchyFilterType filterType) implements EntityMatchCriteria, HierarchyPositionCriteria {

    public SubClassOfCriteria {
        Objects.requireNonNull(target, "Null target");
        Objects.requireNonNull(filterType, "Null filterType");
    }

    private static final String TARGET = "target";

    private static final String FILTER_TYPE = "filterType";

    @JsonCreator
    @Nonnull
    public static SubClassOfCriteria get(@Nonnull @JsonProperty(TARGET) OWLClass target, @Nonnull @JsonProperty(FILTER_TYPE) HierarchyFilterType filterType) {
        return new SubClassOfCriteria(target, filterType);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull HierarchyPositionCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(TARGET)
    @Nonnull
    public OWLClass getTarget() {
        return target;
    }

    @JsonProperty(FILTER_TYPE)
    public HierarchyFilterType getFilterType() {
        return filterType;
    }
}
