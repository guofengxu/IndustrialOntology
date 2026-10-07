package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import javax.annotation.Nonnull;
import java.util.Optional;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.LiteralFormControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public record LiteralFormControlData(@Nonnull OWLLiteral literal) implements PrimitiveFormControlData {

    public LiteralFormControlData {
        Objects.requireNonNull(literal, "Null literal");
    }

    public static LiteralFormControlData get(@Nonnull OWLLiteral literal) {
        return new LiteralFormControlData(literal);
    }

    @JsonIgnore
    public double getValueAsDouble() {
        String lexicalForm = getLiteral().getLiteral();
        return Double.parseDouble(lexicalForm);
    }

    @JsonIgnore
    public boolean isNumber() {
        OWLDatatype datatype = getLiteral().getDatatype();
        if (!datatype.isBuiltIn()) {
            return false;
        }
        return datatype.getBuiltInDatatype().isNumeric();
    }

    @Nonnull
    @Override
    public Optional<OWLEntity> asEntity() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<IRI> asIri() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<OWLLiteral> asLiteral() {
        return Optional.of(getLiteral());
    }

    @Nonnull
    @Override
    public OWLPrimitive getPrimitive() {
        return getLiteral();
    }

    @JsonValue
    @Nonnull
    public OWLLiteral getLiteral() {
        return literal;
    }
}
