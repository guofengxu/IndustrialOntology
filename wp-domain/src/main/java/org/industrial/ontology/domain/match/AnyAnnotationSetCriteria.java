package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyAnnotationSetCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("AnyAnnotationSet")
public record AnyAnnotationSetCriteria() implements AnnotationSetCriteria {

    @JsonCreator
    @Nonnull
    public static AnyAnnotationSetCriteria get() {
        return new AnyAnnotationSetCriteria();
    }
}
