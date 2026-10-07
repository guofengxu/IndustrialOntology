package org.industrial.ontology.domain.change;

import org.industrial.ontology.domain.diff.DiffElement;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.change.ProjectChange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 24/02/15
 */
public record ProjectChange(int changeCount, RevisionNumber revisionNumber, UserId author, String summary, long timestamp, Page<DiffElement<String, String>> diff) implements Serializable {

    public ProjectChange {
        java.util.Objects.requireNonNull(revisionNumber, "Null revisionNumber");
        java.util.Objects.requireNonNull(author, "Null author");
        java.util.Objects.requireNonNull(summary, "Null summary");
        java.util.Objects.requireNonNull(diff, "Null diff");
    }

    @Nonnull
    public static ProjectChange get(@Nonnull RevisionNumber revisionNumber, UserId author, long timestamp, String summary, int changeCount, Page<DiffElement<String, String>> diff) {
        return new ProjectChange(changeCount, revisionNumber, author, summary, timestamp, diff);
    }

    public int getChangeCount() {
        return changeCount;
    }

    public RevisionNumber getRevisionNumber() {
        return revisionNumber;
    }

    public UserId getAuthor() {
        return author;
    }

    public String getSummary() {
        return summary;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Page<DiffElement<String, String>> getDiff() {
        return diff;
    }
}
