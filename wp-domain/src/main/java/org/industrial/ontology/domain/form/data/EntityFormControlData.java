package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonValue;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLPrimitive;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.EntityFormControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public record EntityFormControlData(@Nonnull OWLEntity entity) implements PrimitiveFormControlData {

    public EntityFormControlData {
        Objects.requireNonNull(entity, "Null entity");
    }

    public static EntityFormControlData get(@Nonnull OWLEntity entity) {
        return new EntityFormControlData(entity);
    }

    @Nonnull
    @Override
    public Optional<OWLEntity> asEntity() {
        return Optional.of(getEntity());
    }

    @Nonnull
    @Override
    public Optional<IRI> asIri() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public OWLPrimitive getPrimitive() {
        return getEntity();
    }

    @JsonValue
    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }
}
