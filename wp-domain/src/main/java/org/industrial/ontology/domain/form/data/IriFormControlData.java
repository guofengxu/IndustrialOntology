package org.industrial.ontology.domain.form.data;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLPrimitive;
import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.IriFormControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-07
 */
public record IriFormControlData(@Nonnull IRI iri) implements PrimitiveFormControlData {

    public IriFormControlData {
        Objects.requireNonNull(iri, "Null iri");
    }

    public static IriFormControlData get(@Nonnull IRI iri) {
        return new IriFormControlData(iri);
    }

    @Nonnull
    @Override
    public Optional<OWLEntity> asEntity() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<IRI> asIri() {
        return Optional.of(getIri());
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public OWLPrimitive getPrimitive() {
        return getIri();
    }

    @Nonnull
    public IRI getIri() {
        return iri;
    }
}
