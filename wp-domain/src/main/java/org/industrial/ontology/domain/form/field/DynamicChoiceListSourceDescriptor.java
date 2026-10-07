package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.DynamicChoiceListSourceDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-11
 */
@JsonTypeName(DynamicChoiceListSourceDescriptor.TYPE)
public record DynamicChoiceListSourceDescriptor(@JsonProperty(DynamicChoiceListSourceDescriptor.CRITERIA) @Nonnull EntityMatchCriteria criteria) implements ChoiceListSourceDescriptor {

    public DynamicChoiceListSourceDescriptor {
        Objects.requireNonNull(criteria, "Null criteria");
    }

    public static final String TYPE = "Dynamic";

    private static final String CRITERIA = "criteria";

    @JsonCreator
    public static DynamicChoiceListSourceDescriptor get(@JsonProperty(CRITERIA) @Nonnull EntityMatchCriteria criteria) {
        return new DynamicChoiceListSourceDescriptor(criteria);
    }

    @JsonProperty(CRITERIA)
    @Nonnull
    public EntityMatchCriteria getCriteria() {
        return criteria;
    }
}
