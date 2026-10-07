package org.industrial.ontology.domain.form.data;



import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.industrial.ontology.domain.match.Criteria;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.PrimitiveFormControlDataMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-16
 */
@JsonSubTypes(
        {
                @JsonSubTypes.Type(EntityFormControlDataMatchCriteria.class),
                @JsonSubTypes.Type(LiteralFormControlDataMatchCriteria.class)
        }
)
public interface PrimitiveFormControlDataMatchCriteria extends Criteria {

        <R> R accept(@Nonnull PrimitiveFormControlDataMatchCriteriaVisitor<R> visitor);

}
