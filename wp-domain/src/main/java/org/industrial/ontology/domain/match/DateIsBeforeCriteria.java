package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.DateIsBeforeCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15 Jun 2018
 */
@JsonTypeName("DateIsBefore")
public record DateIsBeforeCriteria(@JsonProperty(YEAR) int year, @JsonProperty(MONTH) int month, @JsonProperty(DAY) int day) implements DateCriteria {

    @Nonnull
    @JsonCreator
    public static DateIsBeforeCriteria get(@JsonProperty(YEAR) int year, @JsonProperty(MONTH) int month, @JsonProperty(DAY) int day) {
        DateCriteria.checkArgs(year, month, day);
        return new DateIsBeforeCriteria(year, month, day);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull LiteralCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    @JsonProperty(YEAR)
    public int getYear() {
        return year;
    }

    @Override
    @JsonProperty(MONTH)
    public int getMonth() {
        return month;
    }

    @Override
    @JsonProperty(DAY)
    public int getDay() {
        return day;
    }
}
