package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.industrial.ontology.domain.core.HasSignature;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collections;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.EntityFrameChangedEvent}.
 * <p>
 * The frame (axioms about one entity) changed; clients refresh that entity's editors.
 */
public sealed interface EntityFrameChangedEvent<E extends OWLEntity> extends ProjectEvent, HasSignature
        permits ClassFrameChangedEvent, ObjectPropertyFrameChangedEvent, DataPropertyFrameChangedEvent, AnnotationPropertyFrameChangedEvent, NamedIndividualFrameChangedEvent, DatatypeFrameChangedEvent {

    E entity();

    /** The user who made the change; {@code null} for changes applied by the system. */
    UserId userId();

    default E getEntity() {
        return entity();
    }

    default UserId getUserId() {
        return userId();
    }

    @JsonIgnore
    @Override
    default Set<OWLEntity> getSignature() {
        return Collections.singleton(entity());
    }
}
