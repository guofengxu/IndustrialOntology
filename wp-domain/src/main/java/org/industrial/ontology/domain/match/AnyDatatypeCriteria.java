package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyDatatypeCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("AnyDatatype")
public record AnyDatatypeCriteria() implements DatatypeCriteria {

    @JsonCreator
    @Nonnull
    public static AnyDatatypeCriteria get() {
        return new AnyDatatypeCriteria();
    }

    @Override
    public <R> R accept(DatatypeCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
