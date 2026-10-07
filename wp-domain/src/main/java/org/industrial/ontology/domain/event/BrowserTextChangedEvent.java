package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.BrowserTextChangedEvent}.
 */
@JsonTypeName("BrowserTextChanged")
public record BrowserTextChangedEvent(@JsonProperty("entity") OWLEntity entity,
        @JsonProperty("newBrowserText") String newBrowserText,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("shortForms") ImmutableMap<DictionaryLanguage, String> shortForms) implements ProjectEvent {

    public BrowserTextChangedEvent {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(newBrowserText, "newBrowserText");
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(shortForms, "shortForms");
    }

    public OWLEntity getEntity() {
        return entity;
    }

    public String getNewBrowserText() {
        return newBrowserText;
    }

    public ImmutableMap<DictionaryLanguage, String> getShortForms() {
        return shortForms;
    }
}
