package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormEntitySubject}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-13
 */
@JsonTypeName("entity")
public record FormEntitySubject(@Nonnull OWLEntity entity) implements FormSubject {

    public FormEntitySubject {
        Objects.requireNonNull(entity, "Null entity");
    }

    public static FormEntitySubject get(@Nonnull OWLEntity entity) {
        return new FormEntitySubject(entity);
    }

    @Override
    public <R> R accept(@Nonnull FormDataSubjectVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormDataSubjectVisitor visitor) {
        visitor.visit(this);
    }

    @Nonnull
    @Override
    public IRI getIri() {
        return getEntity().getIRI();
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }
}
