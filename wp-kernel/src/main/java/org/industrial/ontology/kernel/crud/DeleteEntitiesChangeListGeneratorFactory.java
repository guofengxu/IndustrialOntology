package org.industrial.ontology.kernel.crud;

import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.util.EntityDeleter;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link DeleteEntitiesChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.DeleteEntitiesChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class DeleteEntitiesChangeListGeneratorFactory {

    private final Supplier<MessageFormatter> msgFormatter;

    private final Supplier<EntityDeleter> entityDeleter;

    public DeleteEntitiesChangeListGeneratorFactory(Supplier<MessageFormatter> msgFormatter,
            Supplier<EntityDeleter> entityDeleter) {
        this.msgFormatter = java.util.Objects.requireNonNull(msgFormatter);
        this.entityDeleter = java.util.Objects.requireNonNull(entityDeleter);
    }

    public DeleteEntitiesChangeListGenerator create(@Nonnull Set<OWLEntity> entities) {
        return new DeleteEntitiesChangeListGenerator(msgFormatter.get(), entityDeleter.get(), entities);
    }
}
