package org.industrial.ontology.app.revision;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectDownloadService;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.change.ProjectChange;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.revision.RevisionDetails;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.REVERT_CHANGES;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_CHANGES;

/**
 * A project's revisions and changes (docs/01 §5.1, stage S8): the legacy {@code GetRevisions}, {@code GetRevision},
 * {@code GetRevisionSummaries}, {@code GetHeadRevisionNumber}, {@code GetProjectChanges} and {@code RevertRevision}
 * handlers. Reading needs {@code ViewChanges}, as before; reverting needs {@code RevertChanges} and, as every change
 * does, {@code EditOntology} (the legacy {@code ChangeManager} checked it).
 * <p>
 * Reverting a revision applies the inverse of its changes, newest first, as a new revision; the revision itself stays
 * in the history. The diff between two revisions (docs/02 §5) is new; the governance module of P3 will use it.
 */
public class RevisionService {

    /**
     * The project has no revision with the number.
     */
    public static final String REVISION_NOT_FOUND = ProjectDownloadService.REVISION_NOT_FOUND;

    /**
     * How many added and how many removed axioms a diff lists.
     */
    public static final int MAX_DIFF_AXIOMS = 10_000;

    /**
     * How many of its changes a revision lists.
     */
    public static final int MAX_LISTED_CHANGES = 1000;

    /**
     * How many revisions one call lists; clients page with {@code from}.
     */
    public static final int MAX_LISTED_REVISIONS = 1000;

    private final ProjectContexts projects;

    public RevisionService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The summaries of the revisions numbered from {@code from} to {@code to}, oldest first, optionally only those of
     * one author, at most {@value #MAX_LISTED_REVISIONS}. As in the legacy handler, the range is applied first and the
     * author afterwards.
     *
     * @param from the first revision, 1 or more
     * @param to   the last revision, or {@code null} for the head
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project's changes
     * @throws WpException               {@code INVALID_REQUEST} if {@code from} is below 1 or after {@code to}
     */
    @Nonnull
    public List<RevisionSummary> getRevisions(@Nonnull UserId caller,
                                              @Nonnull ProjectId projectId,
                                              long from,
                                              @Nullable Long to,
                                              @Nonnull Optional<UserId> author) {
        checkNotNull(author);
        if (from < 1) {
            throw WpException.invalidRequest("from is a revision number, 1 or more");
        }
        if (to != null && to < from) {
            throw WpException.invalidRequest("to must not be before from");
        }
        return projects.read(caller, projectId, VIEW_CHANGES,
                             context -> context.revisionManager()
                                               .getRevisionSummaries()
                                               .stream()
                                               .filter(summary -> inRange(summary.getRevisionNumber(), from, to))
                                               .filter(summary -> author.isEmpty()
                                                       || author.get().equals(summary.getUserId()))
                                               .limit(MAX_LISTED_REVISIONS)
                                               .toList());
    }

    private static boolean inRange(RevisionNumber number, long from, @Nullable Long to) {
        return number.getValue() >= from && (to == null || number.getValue() <= to);
    }

    /**
     * The number of the project's latest revision; 0 for a project without revisions.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project's changes (as in the legacy handler)
     */
    @Nonnull
    public RevisionNumber getHeadRevision(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        return projects.read(caller, projectId, VIEW_CHANGES, context -> context.revisionManager().getCurrentRevision());
    }

    /**
     * The revision with the first {@value #MAX_LISTED_CHANGES} axioms it added and removed and their subjects; an
     * import is one revision with every axiom of the ontologies.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project's changes
     * @throws WpException               {@value #REVISION_NOT_FOUND} (404) if the project has no such revision;
     *                                   {@code INVALID_REQUEST} for the head alias
     */
    @Nonnull
    public ListedRevision getRevision(@Nonnull UserId caller,
                                      @Nonnull ProjectId projectId,
                                      @Nonnull RevisionNumber revisionNumber) {
        requireNumbered(revisionNumber);
        return projects.read(caller, projectId, VIEW_CHANGES, context -> {
            var revision = context.revisionManager()
                                  .getRevision(revisionNumber)
                                  .orElseThrow(() -> revisionNotFound(projectId, revisionNumber));
            var listed = revision.getSize() <= MAX_LISTED_CHANGES
                    ? revision
                    : new Revision(revision.getUserId(), revision.getRevisionNumber(),
                                   revision.getChanges().subList(0, MAX_LISTED_CHANGES), revision.getTimestamp(),
                                   revision.getHighLevelDescription());
            return new ListedRevision(context.components().revisionDetailsExtractor().extractRevisionDetails(listed),
                                      revision.getSize());
        });
    }

    /**
     * A revision as {@link #getRevision} lists it.
     *
     * @param details     the revision with at most {@value #MAX_LISTED_CHANGES} of its changes
     * @param changeCount the number of all its changes
     */
    public record ListedRevision(@Nonnull RevisionDetails details, int changeCount) {

        public ListedRevision {
            checkNotNull(details);
        }
    }

    private static void requireNumbered(RevisionNumber revisionNumber) {
        checkNotNull(revisionNumber);
        if (revisionNumber.isHead()) {
            // The store resolves the alias when it is used, which a concurrent change could make another revision
            throw WpException.invalidRequest("A revision number is needed, not the head alias");
        }
    }

    /**
     * A page of the project's changes, newest revision first, rendered as diffs: one entry per revision, or with an
     * entity one entry per revision that changed it (the legacy entity changes view).
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project's changes
     */
    @Nonnull
    public Page<ProjectChange> getChanges(@Nonnull UserId caller,
                                          @Nonnull ProjectId projectId,
                                          @Nonnull Optional<OWLEntity> entity,
                                          @Nonnull PageRequest pageRequest) {
        checkNotNull(entity);
        checkNotNull(pageRequest);
        return projects.read(caller, projectId, VIEW_CHANGES,
                             context -> context.components().projectChangesManager()
                                               .getProjectChanges(entity, pageRequest));
    }

    /**
     * The axioms that the revisions after {@code from} up to {@code to} added and removed overall, in Manchester
     * syntax (docs/02 §5, reserved for the governance module of P3): an axiom added and removed again within the range
     * is in neither list. Each list holds at most {@value #MAX_DIFF_AXIOMS} axioms; the counts are of all of them.
     *
     * @param from the revision before the range, 0 for the empty project
     * @param to   the last revision of the range, or {@code null} for the head
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project's changes
     * @throws WpException               {@code INVALID_REQUEST} if {@code from} is negative or after {@code to};
     *                                   {@value #REVISION_NOT_FOUND} (404) if {@code to} is after the head
     */
    @Nonnull
    public RevisionDiff getDiff(@Nonnull UserId caller, @Nonnull ProjectId projectId, long from, @Nullable Long to) {
        if (from < 0) {
            throw WpException.invalidRequest("from is a revision number, 0 or more");
        }
        if (to != null && to < from) {
            throw WpException.invalidRequest("to must not be before from");
        }
        return projects.read(caller, projectId, VIEW_CHANGES, context -> {
            var head = context.revisionManager().getCurrentRevision().getValue();
            var last = to != null ? to : head;
            if (last > head) {
                throw revisionNotFound(projectId, RevisionNumber.getRevisionNumber(last));
            }
            // Keyed by ontology too: the same axiom in two of the project's ontologies is two axioms
            var added = new LinkedHashSet<OntologyAxiom>();
            var removed = new LinkedHashSet<OntologyAxiom>();
            for (var revision : context.revisionManager().getRevisions()) {
                var number = revision.getRevisionNumber().getValue();
                if (number <= from || number > last) {
                    continue;
                }
                for (var change : revision.getChanges()) {
                    if (change.isAddAxiom()) {
                        var axiom = new OntologyAxiom(change.getOntologyId(), change.getAxiomOrThrow());
                        if (!removed.remove(axiom)) {
                            added.add(axiom);
                        }
                    } else if (change.isRemoveAxiom()) {
                        var axiom = new OntologyAxiom(change.getOntologyId(), change.getAxiomOrThrow());
                        if (!added.remove(axiom)) {
                            removed.add(axiom);
                        }
                    }
                }
            }
            var rendering = context.rendering();
            return new RevisionDiff(from, last, added.size(), removed.size(),
                                    render(added, rendering), render(removed, rendering));
        });
    }

    @SuppressWarnings("deprecation")
    private static List<String> render(Collection<OntologyAxiom> axioms, RenderingManager rendering) {
        return axioms.stream().limit(MAX_DIFF_AXIOMS).map(axiom -> rendering.getBrowserText(axiom.axiom())).toList();
    }

    private record OntologyAxiom(OWLOntologyID ontologyId, OWLAxiom axiom) {
    }

    /**
     * The net axiom changes between two revisions.
     *
     * @param addedAxioms   the first {@value #MAX_DIFF_AXIOMS} added axioms, in Manchester syntax
     * @param removedAxioms the first {@value #MAX_DIFF_AXIOMS} removed axioms, in Manchester syntax
     */
    public record RevisionDiff(long from,
                               long to,
                               int addedCount,
                               int removedCount,
                               @Nonnull List<String> addedAxioms,
                               @Nonnull List<String> removedAxioms) {

        public RevisionDiff {
            addedAxioms = List.copyOf(addedAxioms);
            removedAxioms = List.copyOf(removedAxioms);
        }
    }

    /**
     * Applies the inverse of the revision's changes as a new revision, described "Reverted revision n" unless the
     * caller gives a commit message.
     *
     * @return {@code true}; the outcome has no revision if the revision's changes had already been undone
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not revert changes and edit the ontology
     * @throws WpException               {@value #REVISION_NOT_FOUND} (404) if the project has no such revision
     */
    @Nonnull
    public ChangeOutcome<Boolean> revert(@Nonnull UserId caller,
                                         @Nonnull ProjectId projectId,
                                         @Nonnull RevisionNumber revisionNumber,
                                         @Nullable String commitMessage) {
        requireNumbered(revisionNumber);
        var context = projects.open(caller, projectId, REVERT_CHANGES, EDIT_ONTOLOGY);
        // Revisions are only ever added, so one that exists now still exists when the changes are generated
        requireRevision(context, revisionNumber);
        var generator = context.components()
                               .revisionReverterChangeListGeneratorFactory()
                               .create(revisionNumber);
        return ProjectContexts.apply(context, caller, GuardedChange.of(generator).withCommitMessage(commitMessage));
    }

    private static void requireRevision(ProjectContext context, RevisionNumber revisionNumber) {
        var exists = ProjectContexts.read(context,
                                          reading -> reading.revisionManager().getRevision(revisionNumber).isPresent());
        if (!exists) {
            throw revisionNotFound(context.projectId(), revisionNumber);
        }
    }

    private static WpException revisionNotFound(ProjectId projectId, RevisionNumber revisionNumber) {
        return WpException.notFound(REVISION_NOT_FOUND, "Project " + projectId.getId() + " has no revision "
                + revisionNumber.getValue());
    }
}
