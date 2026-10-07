package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormGroup}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-27
 */
public record FormGroup(@JsonProperty(FormGroup.ID) @Nonnull FormGroupId id, @JsonProperty(FormGroup.DESCRIPTION) @Nonnull String description, @JsonProperty(FormGroup.FORM_IDS) @Nonnull ImmutableList<FormId> formIds) {

    public FormGroup {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(description, "Null description");
        Objects.requireNonNull(formIds, "Null formIds");
    }

    public static final String ID = "id";

    public static final String DESCRIPTION = "description";

    public static final String FORM_IDS = "formIds";

    @JsonCreator
    @Nonnull
    public static FormGroup get(@Nonnull @JsonProperty(ID) FormGroupId formGroupId, @Nonnull @JsonProperty(DESCRIPTION) String description, @Nonnull @JsonProperty(FORM_IDS) ImmutableList<FormId> formIds) {
        return new FormGroup(formGroupId, description, formIds);
    }

    @JsonProperty(ID)
    @Nonnull
    public FormGroupId getId() {
        return id;
    }

    @JsonProperty(DESCRIPTION)
    @Nonnull
    public String getDescription() {
        return description;
    }

    @JsonProperty(FORM_IDS)
    @Nonnull
    public ImmutableList<FormId> getFormIds() {
        return formIds;
    }
}
