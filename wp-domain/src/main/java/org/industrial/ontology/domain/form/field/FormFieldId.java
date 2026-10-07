package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormFieldId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
public record FormFieldId(String id) implements FormRegionId {

    public FormFieldId {
        Objects.requireNonNull(id, "Null id");
    }

    @JsonCreator
    @Nonnull
    public static FormFieldId get(@Nonnull String id) {
        checkFormat(id);
        return new FormFieldId(id);
    }

    private static void checkFormat(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(id)) {
            throw new IllegalArgumentException("Malformed FormFieldId: " + id);
        }
    }

    @Override
    @JsonValue
    public String getId() {
        return id;
    }
}
