package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormSubjectFactoryDescriptor}.
 * <p>
 * Getter auto-detection is off so that the JSON keeps the legacy shape ({@code entityType}, {@code parent},
 * {@code targetOntologyIri}). The legacy {@code *Internal} getters were protected and therefore invisible to Jackson;
 * as record accessors they are public. Hiding {@link #getParent()} with {@code @JsonIgnore} instead would mark the
 * whole {@code parent} property as ignored and drop it on deserialization.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-11
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
public record FormSubjectFactoryDescriptor(@JsonProperty("entityType") @Nonnull EntityType<?> entityType,
                                           @JsonProperty("parent") @Nullable OWLClass parentInternal,
                                           @Nullable IRI targetOntologyIriInternal) {

    public FormSubjectFactoryDescriptor {
        Objects.requireNonNull(entityType, "Null entityType");
    }

    @JsonCreator
    public static FormSubjectFactoryDescriptor get(@Nonnull @JsonProperty("entityType") EntityType entityType, @Nullable @JsonProperty("parent") OWLClass parent, @Nonnull @JsonProperty("targetOntologyIri") Optional<IRI> targetOntologyIri) {
        return new FormSubjectFactoryDescriptor(entityType, parent, targetOntologyIri.orElse(null));
    }

    public static String getDefaultGeneratedNamePattern() {
        return "id-${uuid}";
    }

    /**
     * Gets a list of parents that can be used to position the fresh entity in
     * a hierarchy
     */
    @Nonnull
    public Optional<OWLClass> getParent() {
        return Optional.ofNullable(getParentInternal());
    }

    @JsonProperty("targetOntologyIri")
    @Nonnull
    public Optional<IRI> getTargetOntologyIri() {
        return Optional.ofNullable(getTargetOntologyIriInternal());
    }

    @Nonnull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @JsonProperty("parent")
    @Nullable
    public OWLClass getParentInternal() {
        return parentInternal;
    }

    @Nullable
    public IRI getTargetOntologyIriInternal() {
        return targetOntologyIriInternal;
    }
}
