package org.industrial.ontology.kernel.change;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.domain.core.HasSubject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.Revision;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.ChangeApplicationResult}. Unlike the legacy result,
 * it also carries the revision that the changes were recorded in, which the API returns to the client (docs/02 §1).
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 */
public class ChangeApplicationResult<S> implements HasSubject<S> {

    @Nonnull
    private final RenameMap renameMap;

    @Nonnull
    private final List<OntologyChange> changeList;

    @Nonnull
    private final S subject;

    @Nullable
    private final Revision revision;

    public ChangeApplicationResult(@Nonnull S subject,
                                   @Nonnull List<OntologyChange> changeList,
                                   @Nonnull RenameMap renameMap) {
        this(subject, changeList, renameMap, null);
    }

    private ChangeApplicationResult(@Nonnull S subject,
                                    @Nonnull List<OntologyChange> changeList,
                                    @Nonnull RenameMap renameMap,
                                    @Nullable Revision revision) {
        this.subject = checkNotNull(subject);
        this.changeList = ImmutableList.copyOf(checkNotNull(changeList));
        this.renameMap = checkNotNull(renameMap);
        this.revision = revision;
    }

    /**
     * This result, recorded in the revision.
     */
    @Nonnull
    public ChangeApplicationResult<S> withRevision(@Nonnull Revision revision) {
        return new ChangeApplicationResult<>(subject, changeList, renameMap, checkNotNull(revision));
    }

    /**
     * The revision that recorded the changes; empty when no change was effective, so that nothing was recorded.
     */
    @Nonnull
    public Optional<Revision> getRevision() {
        return Optional.ofNullable(revision);
    }

    @Nonnull
    public RenameMap getRenameMap() {
        return renameMap;
    }

    @Nonnull
    public List<OntologyChange> getChangeList() {
        return changeList;
    }

    @Nonnull
    @Override
    public S getSubject() {
        return subject;
    }
}
