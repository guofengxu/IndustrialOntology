package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.industrial.ontology.domain.util.UUIDUtil;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
public record FormId(String id) implements Serializable {

    public FormId {
        Objects.requireNonNull(id, "Null id");
    }

    @JsonCreator
    public static FormId get(@Nonnull String id) {
        checkFormat(id);
        return new FormId(id);
    }

    @Nonnull
    public static FormId valueOf(@Nonnull String id) {
        return get(id);
    }

    public static FormId generate() {
        return get(UUID.randomUUID().toString());
    }

    public static void checkFormat(@Nonnull String id) {
        if (!UUIDUtil.isWellFormed(id)) {
            throw new RuntimeException("Malformed Form Id.  Form Ids should be UUIDs");
        }
    }

    @JsonValue
    public String getId() {
        return id;
    }
}
