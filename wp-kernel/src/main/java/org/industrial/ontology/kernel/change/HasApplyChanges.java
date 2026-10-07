package org.industrial.ontology.kernel.change;



import org.industrial.ontology.domain.permissions.PermissionDeniedException;
import org.industrial.ontology.domain.core.UserId;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.HasApplyChanges}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/03/2014
 */
public interface HasApplyChanges {
    <R> ChangeApplicationResult<R> applyChanges(UserId userId, ChangeListGenerator<R> changeListGenerator) throws PermissionDeniedException;
}
