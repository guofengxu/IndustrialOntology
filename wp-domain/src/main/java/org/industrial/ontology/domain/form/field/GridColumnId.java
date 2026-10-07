package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.GridColumnId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-25
 */
@JsonTypeName("GridColumnId")
public record GridColumnId(String id) implements FormRegionId {

    public GridColumnId {
        Objects.requireNonNull(id, "Null id");
    }

    @JsonCreator
    @Nonnull
    public static GridColumnId get(@Nonnull String id) {
        checkFormat(id);
        return new GridColumnId(id);
    }

    private static void checkFormat(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(id)) {
            throw new IllegalArgumentException("Malformed GridColumnId: " + id);
        }
    }

    @Override
    @JsonValue
    public String getId() {
        return id;
    }
}
