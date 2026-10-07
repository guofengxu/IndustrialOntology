package org.industrial.ontology.domain.match;



import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.IriCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "match")
@JsonSubTypes({
        @Type(IriEqualsCriteria.class),
        @Type(IriHasAnnotationCriteria.class)
})
public interface IriCriteria extends AnnotationValueCriteria {

}
