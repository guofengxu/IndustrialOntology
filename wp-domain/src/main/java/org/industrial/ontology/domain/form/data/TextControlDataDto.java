package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.TextControlDataDto}.
 */
public record TextControlDataDto(int depth, @JsonProperty("descriptor") @Nonnull TextControlDescriptor descriptor, @JsonProperty("value") @Nullable OWLLiteral valueInternal) implements FormControlDataDto, Comparable<TextControlDataDto> {

    public TextControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    private static Comparator<OWLLiteral> literalComparator = Comparator.nullsLast(Comparator.comparing(OWLLiteral::getLang).thenComparing(OWLLiteral::getLiteral, String::compareToIgnoreCase).thenComparing(OWLLiteral::getDatatype));

    @Nonnull
    public static TextControlDataDto get(@Nonnull TextControlDescriptor descriptor, @Nonnull OWLLiteral value, int depth) {
        return new TextControlDataDto(depth, descriptor, value);
    }

    @JsonIgnore
    @Nonnull
    public Optional<OWLLiteral> getValue() {
        return Optional.ofNullable(getValueInternal());
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public TextControlData toFormControlData() {
        return TextControlData.get(getDescriptor(), getValueInternal());
    }

    @Override
    public int compareTo(@Nonnull TextControlDataDto o) {
        return literalComparator.compare(this.getValueInternal(), o.getValueInternal());
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @JsonProperty("descriptor")
    @Nonnull
    public TextControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("value")
    @Nullable
    public OWLLiteral getValueInternal() {
        return valueInternal;
    }
}
