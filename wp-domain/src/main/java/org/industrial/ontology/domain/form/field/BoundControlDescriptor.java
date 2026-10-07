package org.industrial.ontology.domain.form.field;



import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.BoundControlDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-06
 */
public interface BoundControlDescriptor {

    @Nonnull
    Optional<OwlBinding> getOwlBinding();

    @Nonnull
    FormControlDescriptor getFormControlDescriptor();
}
