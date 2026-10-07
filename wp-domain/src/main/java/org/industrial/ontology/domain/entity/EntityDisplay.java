package org.industrial.ontology.domain.entity;



import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityDisplay}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26 Mar 2017
 */
public interface EntityDisplay {

    void setDisplayedEntity(@Nonnull Optional<OWLEntityData> entityData);
}
