package org.industrial.ontology.domain.form;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.HasFilterState}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-25
 */
public interface HasFilterState {

    @Nonnull
    FilterState getFilterState();
}
