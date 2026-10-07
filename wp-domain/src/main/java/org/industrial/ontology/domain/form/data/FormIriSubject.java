package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormIriSubject}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-13
 */
@JsonTypeName("iri")
public record FormIriSubject(@Nonnull IRI iri) implements FormSubject {

    public FormIriSubject {
        Objects.requireNonNull(iri, "Null iri");
    }

    public static FormIriSubject get(@Nonnull IRI iri) {
        return new FormIriSubject(iri);
    }

    @Override
    public <R> R accept(@Nonnull FormDataSubjectVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormDataSubjectVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    @Nonnull
    public IRI getIri() {
        return iri;
    }
}
