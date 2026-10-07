package org.industrial.ontology.domain.issues.mention;



import org.industrial.ontology.domain.issues.Mention;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.mention.ParsedMention}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 16
 */
public class ParsedMention {

    private Mention parsedMention;

    private int startIndex;

    private int endIndex;

    public ParsedMention(Mention parsedMention, int startIndex, int endIndex) {
        this.parsedMention = parsedMention;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public Mention getParsedMention() {
        return parsedMention;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }


    @Override
    public String toString() {
        return toStringHelper("ParsedMention")
                .add("startIndex", startIndex)
                .add("endIndex", endIndex)
                .addValue(parsedMention)
                .toString();
    }
}
