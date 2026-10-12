package org.industrial.ontology.app.project;

import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A kernel change generator with what a use case adds to it: a check of the state that the client edited, and the
 * client's commit message (docs/02 §1).
 * <p>
 * The check runs when the changes are generated. {@code ChangeManager} generates and applies the changes of one
 * request while holding its change-processing lock, so no other change can come between a passed check and the
 * changes it guards; a failed check throws, and nothing is applied.
 */
public final class GuardedChange<R> implements ChangeListGenerator<R> {

    private final ChangeListGenerator<R> delegate;

    private final Runnable precondition;

    @Nullable
    private final String commitMessage;

    private GuardedChange(ChangeListGenerator<R> delegate, Runnable precondition, @Nullable String commitMessage) {
        this.delegate = checkNotNull(delegate);
        this.precondition = checkNotNull(precondition);
        this.commitMessage = commitMessage;
    }

    /**
     * The generator's changes, without a check and with the generator's own description.
     */
    @Nonnull
    public static <R> GuardedChange<R> of(@Nonnull ChangeListGenerator<R> delegate) {
        return new GuardedChange<>(delegate, () -> { }, null);
    }

    /**
     * Runs the check, which throws to refuse the change, before the changes are generated.
     */
    @Nonnull
    public GuardedChange<R> checking(@Nonnull Runnable precondition) {
        return new GuardedChange<>(delegate, precondition, commitMessage);
    }

    /**
     * Describes the revision with the commit message; a missing or blank message keeps the generator's description.
     */
    @Nonnull
    public GuardedChange<R> withCommitMessage(@Nullable String commitMessage) {
        var message = commitMessage == null || commitMessage.isBlank() ? null : commitMessage.strip();
        return new GuardedChange<>(delegate, precondition, message);
    }

    @Override
    public OntologyChangeList<R> generateChanges(ChangeGenerationContext context) {
        precondition.run();
        return delegate.generateChanges(context);
    }

    @Override
    public R getRenamedResult(R result, RenameMap renameMap) {
        return delegate.getRenamedResult(result, renameMap);
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<R> result) {
        return commitMessage != null ? commitMessage : delegate.getMessage(result);
    }
}
