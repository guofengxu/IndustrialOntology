package org.industrial.ontology.kernel.crud;



import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudKitPlugin}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public interface EntityCrudKitPlugin<H extends EntityCrudKitHandler<S, C>, S extends EntityCrudKitSuffixSettings, C extends ChangeSetEntityCrudSession> {

    EntityCrudKit<S> getEntityCrudKit();

    EntityCrudKitHandler<S, C> getEntityCrudKitHandler();

    EntityCrudKitHandler<S, C> getEntityCrudKitHandler(EntityCrudKitSettings<S> settings);

    S getDefaultSettings();
}
