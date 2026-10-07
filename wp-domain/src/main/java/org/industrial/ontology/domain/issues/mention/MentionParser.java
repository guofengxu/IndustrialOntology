package org.industrial.ontology.domain.issues.mention;



import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import java.util.ArrayList;

import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.mention.MentionParser}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 16
 */
public class MentionParser {

    private static final int START_BOUNDARY_GROUP = 1;

    private static final int ISSUE_MENTION_MATCH_GROUP = 3;

    private static final int USER_ID_MATCH_GROUP = 5;

    private static final int REVISION_MATCH_GROUP = 9;

    private static final int ENTITY_MATCH_GROUP = 11;

    // @formatter:off
    private final Pattern pattern = Pattern.compile(
            // Start of string or some kind of boundary
            "(^|[\\s\\.\\,\\(\\)\\[\\]\\{\\}])" +
                    // Issue e.g. #33 for issue 33
                    "((\\#(\\d+))" +
                    "|" +
                    // UserUd e.g. @Matthew or @{Matthew Horridge}
                    "(\\@(([^\\{\\s,]+)|\\{([^\\}]+)\\}))" +
                    "|" +
                    // Revision e.g. R33 for Revision 33
                    "(R(\\d+))" +
                    "|" +
                    // Entity e.g. Class(<http:/my.ontology/ClassA>)
                    "((Class|ObjectProperty|DataProperty|AnnotationProperty|NamedIndividual|Datatype)\\(<([^>]+)>\\)))",
            // Multiple (the legacy GWT "g" flag is emulated by the find loop below)
            Pattern.MULTILINE);
    // @formatter:on


    public MentionParser() {
    }

    /**
     * Parses a list of Mentions from the given text.
     * @param text The text
     * @return The list of parsed Mentions
     */
    @Nonnull
    public List<ParsedMention> parseMentions(@Nonnull String text) {
        checkNotNull(text);
        List<ParsedMention> parsedMentions = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        int from = 0;
        while (from <= text.length() && matcher.find(from)) {
            MatchResult matchResult = matcher.toMatchResult();
            parsedMentions.add(parseMentionFromMatch(matchResult, text));
            from = matchResult.start() + 1;
        }
        return parsedMentions;
    }

    private static ParsedMention parseMentionFromMatch(MatchResult matchResult, String input) {
        String issueMatchGroup = matchResult.group(ISSUE_MENTION_MATCH_GROUP);
        if (issueMatchGroup != null) {
            return parseIssueMention(matchResult, input);
        }
        String userIdMatchGroup = matchResult.group(USER_ID_MATCH_GROUP);
        if (userIdMatchGroup != null) {
            return parseUserIdMention(matchResult, input);
        }
        String revisionMatchGroup = matchResult.group(REVISION_MATCH_GROUP);
        if (revisionMatchGroup != null) {
            return parseRevisionMention(matchResult, input);
        }
        String entityMatchGroup = matchResult.group(ENTITY_MATCH_GROUP);
        if (entityMatchGroup != null) {
            return parseEntityMention(matchResult, input);
        }
        throw new RuntimeException("MentionParseError:  Match does not correspond to any mention patterns");
    }


    private static ParsedMention parseIssueMention(MatchResult matchResult, String input) {
        String issueMatchGroup = matchResult.group(ISSUE_MENTION_MATCH_GROUP);
        if (issueMatchGroup == null) {
            throw new IllegalStateException("Revision match group is not present in input: " + input);
        }
        int startIndex = matchResult.start() + matchResult.group(START_BOUNDARY_GROUP).length();
        int endIndex = startIndex + issueMatchGroup.length();
        String issueNumberGroup = matchResult.group(ISSUE_MENTION_MATCH_GROUP + 1);
        return new ParsedMention(
                new IssueMention(Integer.parseInt(issueNumberGroup)),
                startIndex,
                endIndex);
    }

    private static ParsedMention parseRevisionMention(MatchResult matchResult, String input) {
        String revisionMatchGroup = matchResult.group(REVISION_MATCH_GROUP);
        if (revisionMatchGroup == null) {
            throw new IllegalStateException("Revision match group is not present in input: " + input);
        }
        int startIndex = matchResult.start() + matchResult.group(START_BOUNDARY_GROUP).length();
        int endIndex = startIndex + revisionMatchGroup.length();
        String revisionNumberGroup = matchResult.group(REVISION_MATCH_GROUP + 1);
        return new ParsedMention(
                new RevisionMention(
                        Long.parseLong(revisionNumberGroup)),
                startIndex,
                endIndex);
    }


    private static ParsedMention parseUserIdMention(MatchResult matchResult, String input) {
        String userIdMatchGroup = matchResult.group(USER_ID_MATCH_GROUP);
        if (userIdMatchGroup == null) {
            throw new IllegalStateException("UserId match group is not present in input: " + input);
        }
        int startIndex = matchResult.start() + matchResult.group(START_BOUNDARY_GROUP).length();
        int endIndex = startIndex + userIdMatchGroup.length();
        String plainUserIdMatchGroup = matchResult.group(USER_ID_MATCH_GROUP + 2);
        if (plainUserIdMatchGroup != null) {
            return new ParsedMention(
                    new UserIdMention(
                            UserId.getUserId(plainUserIdMatchGroup)),
                    startIndex,
                    startIndex + userIdMatchGroup.length());
        }
        else {
            String bracketedUserIdMatchGroup = matchResult.group(USER_ID_MATCH_GROUP + 3);
            if (bracketedUserIdMatchGroup != null) {
                return new ParsedMention(
                        new UserIdMention(
                                UserId.getUserId(bracketedUserIdMatchGroup)),
                        startIndex,
                        endIndex
                );

            }
            else {
                throw new IllegalStateException("UserId not present in input " + input);
            }
        }
    }


    private static ParsedMention parseEntityMention(MatchResult matchResult, String input) {
        String entityMatchGroup = matchResult.group(ENTITY_MATCH_GROUP);
        if (entityMatchGroup == null) {
            throw new IllegalStateException("Entity match group is not present in input: " + input);
        }
        String entityTypeGroup = matchResult.group(ENTITY_MATCH_GROUP + 1);
        if (entityTypeGroup == null) {
            throw new IllegalStateException("EntityType group is not present in input: " + input);
        }
        String entityIriGroup = matchResult.group(ENTITY_MATCH_GROUP + 2);
        if (entityIriGroup == null) {
            throw new IllegalStateException("Entity IRI group is not present in input: " + input);
        }
        EntityType<?> entityType = null;
        for (EntityType<?> type : EntityType.values()) {
            if (type.getName().equals(entityTypeGroup)) {
                entityType = type;
                break;
            }
        }
        if (entityType == null) {
            throw new IllegalStateException("Illegal entity type: " + entityTypeGroup);
        }
        IRI entityIri = IRI.create(entityIriGroup);
        int startIndex = matchResult.start() + matchResult.group(START_BOUNDARY_GROUP).length();
        int endIndex = startIndex + entityMatchGroup.length();
        return new ParsedMention(
                new EntityMention(
                        DataFactory.getOWLEntity(entityType, entityIri)
                ),
                startIndex,
                endIndex
        );

    }
}
