package org.industrial.ontology.domain.issues.mention;



import org.industrial.ontology.domain.issues.Mention;
import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.mention.RevisionMention}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 16
 */
public class RevisionMention extends Mention {

    private long revisionNumber;


    public RevisionMention(long revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    private RevisionMention() {
    }

    public long getRevisionNumber() {
        return revisionNumber;
    }


    @Override
    public String toString() {
        return toStringHelper("RevisionMention")
                .addValue(revisionNumber)
                .toString();
    }
}
