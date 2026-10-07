package org.industrial.ontology.kernel.revision;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.HasDispose;
import org.industrial.ontology.domain.revision.RevisionNumber;
import javax.annotation.Nonnull;

import java.util.Optional;
import org.industrial.ontology.kernel.api.revision.Revision;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionStore}.
 * <p>
 * The legacy {@code addRevision}/{@code getCurrentRevisionNumber} are renamed to {@link #append}/{@link #getHead},
 * and {@link #load()} joins the interface so that {@code ProjectContextFactory} can load any store (docs/01 §3.1).
 * {@link #dispose()} waits for pending writes but must not shut down an executor shared with other projects.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 29/05/15
 */
public interface RevisionStore extends HasDispose {

    /**
     * Reads the persisted change history, replacing any revisions held in memory. A missing history leaves the
     * store empty.
     */
    void load();

    /**
     * Gets all of the revisions.
     * @return The revisions in an immutable list.
     */
    @Nonnull
    ImmutableList<Revision> getRevisions();

    /**
     * Gets the revision that has the specified revision number.
     * @param revisionNumber The revision number.  Not {@code null}.
     * @return The Revision.  If a revision with the specified revision number does not exist
     * then an absent value will be returned.  Not {@code null}.
     */
    @Nonnull
    Optional<Revision> getRevision(@Nonnull RevisionNumber revisionNumber);

    /**
     * Appends the specified revision to this revision store and persists it.  The revision must have a number that
     * is beyond the head revision number otherwise an IllegalArgumentException will be thrown.
     * @param revision The revision to be added.  Not {@code null}.
     */
    void append(@Nonnull Revision revision);

    /**
     * Gets the revision number of the latest revision.
     * @return The revision number of the latest revision.  If there are no revisions then a revision number
     * of zero is returned.
     */
    @Nonnull
    RevisionNumber getHead();
}
