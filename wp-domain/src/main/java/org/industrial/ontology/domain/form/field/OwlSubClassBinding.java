package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.OwlSubClassBinding}.
 */
@JsonTypeName(OwlSubClassBinding.TYPE)
public record OwlSubClassBinding() implements OwlBinding {

    public static final String TYPE = "SUBCLASS";

    @JsonCreator
    @Nonnull
    public static OwlSubClassBinding get() {
        return new OwlSubClassBinding();
    }

    @Nonnull
    @Override
    public Optional<OWLProperty> getOwlProperty() {
        return Optional.empty();
    }
}
