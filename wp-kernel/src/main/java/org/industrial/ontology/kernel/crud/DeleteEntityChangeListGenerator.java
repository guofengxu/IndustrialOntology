package org.industrial.ontology.kernel.crud;



import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.util.EntityDeleter;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collections;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.DeleteEntityChangeListGenerator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 27/03/2013
 */
public class DeleteEntityChangeListGenerator implements ChangeListGenerator<OWLEntity> {

    @Nonnull
    private final OWLEntity entity;

    @Nonnull
    private final EntityDeleter entityDeleter;

    public DeleteEntityChangeListGenerator(@Nonnull OWLEntity entity,
                                           @Nonnull EntityDeleter entityDeleter) {
        this.entity = checkNotNull(entity);
        this.entityDeleter = checkNotNull(entityDeleter);
    }

    @Override
    public OntologyChangeList<OWLEntity> generateChanges(ChangeGenerationContext context) {
        var deletionChanges = entityDeleter.getChangesToDeleteEntities(Collections.singleton(entity));
        var changeListBuilder = new OntologyChangeList.Builder<OWLEntity>();
        changeListBuilder.addAll(deletionChanges);
        return changeListBuilder.build(entity);
    }

    @Override
    public OWLEntity getRenamedResult(OWLEntity result, RenameMap renameMap) {
        return renameMap.getRenamedEntity(result);
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<OWLEntity> result) {
        return "Deleted " + entity.getEntityType().getPrintName().toLowerCase();
    }
}
