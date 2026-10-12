package org.industrial.ontology.app.frame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.frame.HasFreshEntities;
import org.industrial.ontology.domain.frame.ManchesterSyntaxFrameParseError;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.EntityNameMatchResult;
import org.industrial.ontology.domain.search.EntityNameMatcher;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.shortform.SearchString;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.mansyntax.ManchesterSyntaxFrameParser;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.render.ManchesterSyntaxKeywords;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxFrameRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * Entity frames in Manchester syntax (docs/01 §5.1, stage S7): the legacy {@code GetManchesterSyntaxFrame},
 * {@code CheckManchesterSyntaxFrame}, {@code SetManchesterSyntaxFrame} and {@code GetManchesterSyntaxFrameCompletions}
 * handlers.
 * <p>
 * Reading, checking and completing need {@code ViewProject} (the legacy frame and completion handlers checked
 * nothing); setting needs {@code EditOntology}. Names that the frame text uses for entities that do not exist yet can
 * be declared as fresh entities of a type; setting the frame creates them, which needs the {@code CREATE_*} permission
 * of their type. Line and column numbers start at 1 (OWL API starts the columns of all but the first line at 0).
 */
public class ManchesterSyntaxService {

    public static final String MANCHESTER_SYNTAX_ERROR = "MANCHESTER_SYNTAX_ERROR";

    /** How many matching entities the dictionary is asked for before they are ranked (legacy {@code SEARCH_LIMIT}). */
    private static final int ENTITY_SEARCH_LIMIT = 3000;

    private static final int MAX_ENTITY_CHOICES = 100;

    private final ProjectContexts projects;

    private final ManchesterSyntaxKeywords keywords = new ManchesterSyntaxKeywords();

    public ManchesterSyntaxService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The entity's frame as Manchester syntax text, with the axioms of all the project's ontologies.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public Frame getFrame(@Nonnull UserId caller, @Nonnull ProjectId projectId, @Nonnull OWLEntity subject) {
        return projects.read(caller, projectId, VIEW_PROJECT,
                             context -> new Frame(context.rendering().getRendering(subject),
                                                  frameText(context, subject)));
    }

    /**
     * Whether the edit of the frame text from {@code from} to {@code to} parses and changes anything; nothing is
     * changed.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public Check check(@Nonnull UserId caller,
                       @Nonnull ProjectId projectId,
                       @Nonnull OWLEntity subject,
                       @Nonnull String from,
                       @Nonnull String to,
                       @Nonnull Set<FreshEntity> freshEntities) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var generator = context.components()
                                   .manchesterSyntaxChangeGeneratorFactory()
                                   .create(context.rendering().getRendering(subject),
                                           from,
                                           to,
                                           "",
                                           freshEntities(context, freshEntities));
            var changes = generator.generateChanges(
                    new ChangeGenerationContext(caller));
            var error = changes.getResult();
            if (error.isPresent()) {
                return new Check(CheckStatus.ERROR, withColumnFromOne(error.get()));
            }
            return new Check(changes.getChanges().isEmpty() ? CheckStatus.UNCHANGED : CheckStatus.CHANGED, null);
        });
    }

    /**
     * Replaces the frame text {@code from} with {@code to} in one revision by the caller: the axioms of {@code from}
     * that are not in {@code to} are removed and the others added.
     *
     * @param commitMessage added to the description of the revision, "Edited description of ..." as before
     * @return the frame text after the change, formatted as {@link #getFrame} formats it
     * @throws ProjectNotFoundException      if the project does not exist
     * @throws PermissionDeniedException     if the caller may not edit the ontology, or may not create the fresh
     *                                       entities
     * @throws ManchesterSyntaxFrameException {@value #MANCHESTER_SYNTAX_ERROR} (400) if {@code from} or {@code to}
     *                                       does not parse; nothing is changed
     */
    @Nonnull
    public ChangeOutcome<String> setFrame(@Nonnull UserId caller,
                                          @Nonnull ProjectId projectId,
                                          @Nonnull OWLEntity subject,
                                          @Nonnull String from,
                                          @Nonnull String to,
                                          @Nullable String commitMessage,
                                          @Nonnull Set<FreshEntity> freshEntities) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        var generator = context.components()
                               .manchesterSyntaxChangeGeneratorFactory()
                               .create(context.rendering().getRendering(subject),
                                       from,
                                       to,
                                       Objects.requireNonNullElse(commitMessage, "").strip(),
                                       freshEntities(context, freshEntities));
        var outcome = ProjectContexts.apply(context, caller, generator);
        outcome.result().ifPresent(error -> {
            throw new ManchesterSyntaxFrameException(withColumnFromOne(error));
        });
        return outcome.map(noError -> ProjectContexts.read(context, ctx -> frameText(ctx, subject)));
    }

    /**
     * What may be typed at the cursor of the frame text: the entities, keywords and ontology names that the parser
     * expects there and that match the word before the cursor, entities first. Nothing is proposed where the text up
     * to the cursor parses.
     *
     * @param cursor the offset of the cursor in the text
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for a cursor outside the text
     */
    @Nonnull
    public Completions getCompletions(@Nonnull UserId caller,
                                      @Nonnull ProjectId projectId,
                                      @Nonnull String text,
                                      int cursor,
                                      @Nonnull Set<FreshEntity> freshEntities,
                                      int maxEntityChoices) {
        if (cursor < 0 || cursor > text.length()) {
            throw WpException.invalidRequest("The cursor must be within the text");
        }
        var limit = Math.max(0, Math.min(maxEntityChoices, MAX_ENTITY_CHOICES));
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var parser = context.components().newManchesterSyntaxFrameParser();
            try {
                // The NUL character makes the parser fail at the cursor and report what it expects there
                parser.parse(text.substring(0, cursor) + "\u0000", freshEntities(context, freshEntities));
            } catch (ParserException e) {
                var wordStart = lastWordStart(text, cursor);
                var wordEnd = wordEnd(text, cursor);
                var prefix = text.substring(wordStart, cursor).toLowerCase();
                var choices = new ArrayList<Choice>();
                choices.addAll(entityChoices(context, e, prefix, limit));
                choices.addAll(keywordChoices(e, prefix));
                choices.addAll(ontologyNameChoices(context, e, prefix));
                return new Completions(choices, position(text, wordStart), position(text, wordEnd));
            }
            var position = position(text, cursor);
            return new Completions(List.of(), position, position);
        });
    }

    private static String frameText(ProjectContext context, OWLEntity subject) {
        var components = context.components();
        var facades = context.indexes()
                             .get(ProjectOntologiesIndex.class)
                             .getOntologyIds()
                             .map(components.ontologyFacadeFactory()::create)
                             .collect(Collectors.<OWLOntology>toSet());
        var writer = new StringWriter();
        var frameRenderer = new ManchesterOWLSyntaxFrameRenderer(facades, writer,
                                                                 components.newEscapingShortFormProvider());
        frameRenderer.setOntologyIRIShortFormProvider(components.ontologyIriShortFormProvider());
        frameRenderer.setRenderExtensions(true);
        frameRenderer.writeFrame(subject);
        return writer.toString();
    }

    /**
     * The fresh entities as the parser looks them up: by name, with IRIs that {@code ChangeManager} replaces with
     * real ones when it creates the entities.
     */
    private static HasFreshEntities freshEntities(ProjectContext context, Set<FreshEntity> freshEntities) {
        var dataFactory = context.dataFactory();
        var data = freshEntities.stream()
                                .map(fresh -> DataFactory.getOWLEntityData(
                                        DataFactory.getFreshOWLEntity(fresh.type(),
                                                                      fresh.name(),
                                                                      Optional.empty(),
                                                                      dataFactory),
                                        ImmutableMap.of(DictionaryLanguage.localName(), fresh.name())))
                                .collect(ImmutableSet.toImmutableSet());
        return () -> data;
    }

    private List<Choice> entityChoices(ProjectContext context, ParserException e, String prefix, int limit) {
        var expectedTypes = Set.copyOf(ManchesterSyntaxFrameParser.getExpectedEntityTypes(e));
        if (expectedTypes.isEmpty() || prefix.isEmpty() || limit == 0) {
            return List.of();
        }
        var dictionary = context.dictionary();
        var shortForms = context.components().newEscapingShortFormProvider();
        var matcher = new EntityNameMatcher(prefix);
        var matches = dictionary.getShortFormsContaining(List.of(SearchString.parseSearchString(prefix)),
                                                         expectedTypes,
                                                         context.languageManager().getLanguages(),
                                                         ImmutableList.of(),
                                                         PageRequest.requestPageWithSize(1, ENTITY_SEARCH_LIMIT));
        var ranked = new LinkedHashMap<OWLEntity, EntityNameMatchResult>();
        for (var match : matches.getPageElements()) {
            var entity = match.getEntity();
            if (ranked.containsKey(entity)) {
                continue;
            }
            matcher.findIn(dictionary.getShortForm(entity)).ifPresent(result -> ranked.put(entity, result));
        }
        return ranked.entrySet()
                     .stream()
                     .sorted(Map.Entry.<OWLEntity, EntityNameMatchResult>comparingByValue()
                                      .thenComparing(entry -> dictionary.getShortForm(entry.getKey()),
                                                     String.CASE_INSENSITIVE_ORDER))
                     .limit(limit)
                     .map(entry -> new Choice(shortForms.getShortForm(entry.getKey()),
                                              dictionary.getShortForm(entry.getKey()),
                                              ""))
                     .collect(toImmutableList());
    }

    private List<Choice> keywordChoices(ParserException e, String prefix) {
        var comparator = new ManchesterSyntaxKeywords.KeywordComparator();
        return e.getExpectedKeywords()
                .stream()
                .filter(keyword -> prefix.isEmpty() || keyword.toLowerCase().contains(prefix))
                .sorted(comparator)
                .map(keyword -> new Choice(keyword,
                                           keyword,
                                           keywords.getKeyword(keyword).map(keywords::getStyleName).orElse("")))
                .collect(toImmutableList());
    }

    private static List<Choice> ontologyNameChoices(ProjectContext context, ParserException e, String prefix) {
        if (!e.isOntologyNameExpected()) {
            return List.of();
        }
        var shortForms = context.components().ontologyIriShortFormProvider();
        return context.indexes()
                      .get(ProjectOntologiesIndex.class)
                      .getOntologyIds()
                      .map(shortForms::getShortForm)
                      .filter(shortForm -> prefix.isEmpty() || shortForm.toLowerCase().startsWith(prefix))
                      .sorted(Comparator.naturalOrder())
                      .map(shortForm -> new Choice(shortForm, shortForm, "cm-ontology-list"))
                      .collect(toImmutableList());
    }

    /**
     * The error with its column counted from 1. OWL API's Manchester syntax tokenizer counts the columns of the first
     * line from 1 and those of the other lines from 0.
     */
    private static ManchesterSyntaxFrameParseError withColumnFromOne(ManchesterSyntaxFrameParseError error) {
        if (error.getLine() <= 1) {
            return error;
        }
        return new ManchesterSyntaxFrameParseError(error.getMessage(),
                                                   error.getCol() + 1,
                                                   error.getLine(),
                                                   error.getToken(),
                                                   error.getExpectedEntityTypes());
    }

    /**
     * The start of the word that ends at the cursor; the legacy handler returned -1 for a word at the start of the
     * text, and then failed.
     */
    private static int lastWordStart(String text, int cursor) {
        for (int i = cursor - 1; i >= 0; i--) {
            if (isNonWordChar(text.charAt(i))) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * The end of the word at the cursor; the legacy handler always returned the cursor.
     */
    private static int wordEnd(String text, int cursor) {
        for (int i = cursor; i < text.length(); i++) {
            if (isNonWordChar(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    private static boolean isNonWordChar(char ch) {
        return ch == ' ' || ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == '(' || ch == ')' || ch == ','
                || ch == '\'' || ch == '\t' || ch == '\n' || ch == '\r' || ch == '^';
    }

    private static Position position(String text, int offset) {
        int line = 1;
        int lineStart = 0;
        for (int i = 0; i < offset; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                lineStart = i + 1;
            }
        }
        return new Position(line, offset - lineStart + 1);
    }

    /**
     * An entity's frame in Manchester syntax.
     */
    public record Frame(@Nonnull OWLEntityData subject, @Nonnull String text) {
    }

    public enum CheckStatus {
        /** The edit parses and changes axioms. */
        CHANGED,
        /** The edit parses but changes no axiom. */
        UNCHANGED,
        /** The edit does not parse. */
        ERROR
    }

    /**
     * The result of checking an edit; the error is present when the status is {@link CheckStatus#ERROR}.
     */
    public record Check(@Nonnull CheckStatus status, @Nullable ManchesterSyntaxFrameParseError error) {
    }

    /**
     * A name that the frame text uses for an entity of the type that does not exist yet.
     */
    public record FreshEntity(@Nonnull String name, @Nonnull EntityType<?> type) {

        public FreshEntity {
            checkNotNull(name);
            checkNotNull(type);
            if (name.isBlank()) {
                throw WpException.invalidRequest("A fresh entity needs a name");
            }
        }
    }

    /**
     * A place in the text.
     */
    public record Position(int line, int column) {
    }

    /**
     * Text that may replace the word around the cursor, between the {@link Completions}' {@code from} and {@code to}.
     *
     * @param style the CodeMirror style of a keyword, empty for entities
     */
    public record Choice(@Nonnull String text, @Nonnull String displayText, @Nonnull String style) {
    }

    /**
     * The completions at the cursor, which replace the text between {@code from} and {@code to}.
     */
    public record Completions(@Nonnull List<Choice> choices, @Nonnull Position from, @Nonnull Position to) {
    }

    /**
     * A frame text that does not parse: 400 {@value #MANCHESTER_SYNTAX_ERROR}, with where and what was expected.
     */
    public static class ManchesterSyntaxFrameException extends WpException {

        private final ManchesterSyntaxFrameParseError error;

        public ManchesterSyntaxFrameException(@Nonnull ManchesterSyntaxFrameParseError error) {
            super(MANCHESTER_SYNTAX_ERROR, 400, error.getMessage());
            this.error = checkNotNull(error);
        }

        @Nonnull
        public ManchesterSyntaxFrameParseError getError() {
            return error;
        }

        @Nonnull
        @Override
        public Map<String, Object> getProperties() {
            return Map.of("line", error.getLine(),
                          "column", error.getCol(),
                          "token", Objects.requireNonNullElse(error.getToken(), ""),
                          "expectedEntityTypes", error.getExpectedEntityTypes()
                                                      .stream()
                                                      .map(EntityType::getName)
                                                      .toList());
        }
    }
}
