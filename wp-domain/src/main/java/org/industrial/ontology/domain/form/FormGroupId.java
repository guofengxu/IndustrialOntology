package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormGroupId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-27
 */
public record FormGroupId(@Nonnull String id) {

    public FormGroupId {
        Objects.requireNonNull(id, "Null id");
    }

    @JsonCreator
    public static FormGroupId get(@Nonnull String id) {
        checkFormat(id);
        return new FormGroupId(id);
    }

    public static FormGroupId generate() {
        return get(UUID.randomUUID().toString());
    }

    public static void checkFormat(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(id)) {
            throw new RuntimeException("Malformed FormGroupId.  FormGroupIds should be UUIDs");
        }
    }

    @JsonValue
    @Nonnull
    public String getId() {
        return id;
    }
}
