package org.industrial.ontology.app.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.search.RegexBudget;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.entity.FreshEntityIri;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.SearchResultMatch;
import org.industrial.ontology.domain.search.SearchResultMatchPosition;
import org.industrial.ontology.domain.usage.UsageFilter;
import org.industrial.ontology.domain.usage.UsageReference;
import org.industrial.ontology.domain.usage.UsageReferenceComparator;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.pagination.PageCollector;
import org.industrial.ontology.kernel.api.shortform.SearchString;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.CREATE_CLASS;
import static org.industrial.ontology.domain.core.BuiltInAction.CREATE_INDIVIDUAL;
import static org.industrial.ontology.domain.core.BuiltInAction.CREATE_PROPERTY;
import static org.industrial.ontology.domain.core.BuiltInAction.DELETE_CLASS;
import static org.industrial.ontology.domain.core.BuiltInAction.DELETE_DATATYPE;
import static org.industrial.ontology.domain.core.BuiltInAction.DELETE_INDIVIDUAL;
import static org.industrial.ontology.domain.core.BuiltInAction.DELETE_PROPERTY;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.MERGE_ENTITIES;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * Creating, deleting, merging, renaming and finding a project's entities (docs/01 §5.1, stage S7): the legacy
 * {@code Create{Classes,ObjectProperties,DataProperties,AnnotationProperties,NamedIndividuals}},
 * {@code DeleteEntities}, {@code MergeEntities}, {@code ChangeEntityIRI}, {@code LookupEntities},
 * {@code GetDeprecatedEntities} and {@code GetUsage} handlers.
 * <p>
 * Every change needs {@code EditOntology}, which the legacy {@code ChangeManager} checked for all of them, and the
 * action of the use case: creating needs the {@code CREATE_*} action of the type (the legacy data property handler
 * did not ask for {@code EditOntology} itself), deleting the {@code DELETE_*} action of each type deleted (docs/02
 * §4.1; the legacy handler asked only for {@code EditOntology}), merging {@code MergeEntities}. Reading needs
 * {@code ViewProject}; the legacy usage handler checked nothing.
 */
public class EntityService {

    public static final String ENTITY_NOT_FOUND = "ENTITY_NOT_FOUND";

    public static final String IRI_IN_USE = "IRI_IN_USE";

    private static final int MAX_LOOKUP_RESULTS = 100;

    private final ProjectContexts projects;

    private final DiscussionThreadRepository discussionThreads;

    public EntityService(@Nonnull ProjectContexts projects, @Nonnull DiscussionThreadRepository discussionThreads) {
        this.projects = checkNotNull(projects);
        this.discussionThreads = checkNotNull(discussionThreads);
    }

    /**
     * Creates an entity of the type for each line of the source text, named after the line, below the parents (the
     * types of new individuals), in one revision by the caller. A line that names an existing entity of the type
     * creates nothing new; the new entities get their IRIs from the project's new entity settings.
     *
     * @param langTag       the language of the names, or empty
     * @param commitMessage describes the revision; without one it is described from its changes
     * @return the entities named by the lines, by display name
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology or create entities of the type
     * @throws WpException               {@code INVALID_REQUEST} for a datatype, a blank source text or a parent
     *                                   that is not a class (for classes and individuals) or a property of the type
     */
    @Nonnull
    public ChangeOutcome<List<EntityNode>> createEntities(@Nonnull UserId caller,
                                                         @Nonnull ProjectId projectId,
                                                         @Nonnull EntityType<?> type,
                                                         @Nonnull String sourceText,
                                                         @Nonnull String langTag,
                                                         @Nonnull Set<? extends OWLEntity> parents,
                                                         @Nullable String commitMessage) {
        var createAction = createAction(type);
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY, createAction);
        if (sourceText.isBlank()) {
            throw WpException.invalidRequest("The source text needs at least one name");
        }
        var parentType = type.equals(EntityType.NAMED_INDIVIDUAL) ? EntityType.CLASS : type;
        if (parents.stream().anyMatch(parent -> !parent.getEntityType().equals(parentType))) {
            throw WpException.invalidRequest("The parents of new " + type.getPluralPrintName().toLowerCase()
                                                     + " must be " + parentType.getPluralPrintName().toLowerCase());
        }
        var components = context.components();
        ChangeOutcome<? extends Set<? extends OWLEntity>> outcome;
        if (type.equals(EntityType.CLASS)) {
            outcome = apply(context, caller, components.createClassesChangeGeneratorFactory()
                                                       .create(sourceText, langTag, cast(parents, OWLClass.class)),
                            commitMessage);
        } else if (type.equals(EntityType.OBJECT_PROPERTY)) {
            outcome = apply(context, caller, components.createObjectPropertiesChangeGeneratorFactory()
                                                       .create(sourceText, langTag,
                                                               cast(parents, OWLObjectProperty.class)),
                            commitMessage);
        } else if (type.equals(EntityType.DATA_PROPERTY)) {
            outcome = apply(context, caller, components.createDataPropertiesChangeGeneratorFactory()
                                                       .create(sourceText, langTag,
                                                               cast(parents, OWLDataProperty.class)),
                            commitMessage);
        } else if (type.equals(EntityType.ANNOTATION_PROPERTY)) {
            outcome = apply(context, caller, components.createAnnotationPropertiesChangeGeneratorFactory()
                                                       .create(sourceText, langTag,
                                                               cast(parents, OWLAnnotationProperty.class)),
                            commitMessage);
        } else {
            outcome = apply(context, caller, components.createIndividualsChangeListGeneratorFactory()
                                                       .create(cast(parents, OWLClass.class), sourceText, langTag),
                            commitMessage);
        }
        return outcome.map(entities -> ProjectContexts.read(context, ctx -> renderNodes(ctx, entities)));
    }

    /**
     * Deletes the entities and every axiom that mentions them, in one revision by the caller.
     *
     * @return the entities deleted
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology or delete entities of a type
     * @throws WpException               {@code INVALID_REQUEST} for no entities, or for a built-in entity such as
     *                                   {@code owl:Thing}
     */
    @Nonnull
    public ChangeOutcome<Set<OWLEntity>> deleteEntities(@Nonnull UserId caller,
                                                        @Nonnull ProjectId projectId,
                                                        @Nonnull Set<OWLEntity> entities) {
        if (entities.isEmpty()) {
            throw WpException.invalidRequest("Name the entities to delete");
        }
        var actions = new LinkedHashSet<BuiltInAction>();
        actions.add(EDIT_ONTOLOGY);
        entities.forEach(entity -> actions.add(deleteAction(entity.getEntityType())));
        var context = projects.open(caller, projectId, actions.toArray(BuiltInAction[]::new));
        requireNoBuiltInEntity(entities);
        var generator = context.components().deleteEntitiesChangeListGeneratorFactory().create(Set.copyOf(entities));
        return ProjectContexts.apply(context, caller, generator);
    }

    /**
     * Merges the source entities into the target, in one revision by the caller: their IRIs are replaced with the
     * target's everywhere, and their names become alternative names of the target. With
     * {@link MergedEntityTreatment#DEPRECATE_MERGED_ENTITY} they are kept, deprecated.
     *
     * @param commitMessage describes the revision; without one it says what was merged into what
     * @return the target
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology or merge entities
     * @throws WpException               {@code INVALID_REQUEST} for no sources, a source that is the target or of
     *                                   another type than the target, or a built-in entity
     */
    @Nonnull
    public ChangeOutcome<OWLEntity> mergeEntities(@Nonnull UserId caller,
                                                  @Nonnull ProjectId projectId,
                                                  @Nonnull Set<OWLEntity> sources,
                                                  @Nonnull OWLEntity target,
                                                  @Nonnull MergedEntityTreatment treatment,
                                                  @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY, MERGE_ENTITIES);
        if (sources.isEmpty()) {
            throw WpException.invalidRequest("Name the entities to merge into the target");
        }
        if (sources.contains(target)) {
            throw WpException.invalidRequest("An entity cannot be merged into itself");
        }
        if (sources.stream().anyMatch(source -> !source.getEntityType().equals(target.getEntityType()))) {
            throw WpException.invalidRequest("Only entities of the target's type can be merged into it");
        }
        var involved = new HashSet<OWLEntity>(sources);
        involved.add(target);
        requireNoBuiltInEntity(involved);
        var message = commitMessage == null || commitMessage.isBlank()
                ? "Merged " + sources.stream().map(source -> source.getIRI().toString()).sorted()
                                     .collect(Collectors.joining(", ")) + " into " + target.getIRI()
                : commitMessage.strip();
        var generator = context.components()
                               .mergeEntitiesChangeListGeneratorFactory()
                               .create(ImmutableSet.copyOf(sources), target, treatment, message);
        return ProjectContexts.apply(context, caller, generator);
    }

    /**
     * Gives the entity a new IRI in every axiom that mentions it, in one revision by the caller, and moves its
     * discussion threads to the renamed entity, as before.
     *
     * @param commitMessage describes the revision; without one it says which IRI became which
     * @return the entity before and after
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} for the entity's own IRI or a built-in entity;
     *                                   {@value #ENTITY_NOT_FOUND} (404) if the project does not have the entity;
     *                                   {@value #IRI_IN_USE} (409) if an entity of the same type already has the IRI
     *                                   (renaming would merge the two; merge them instead)
     */
    @Nonnull
    public ChangeOutcome<RenamedEntity> changeEntityIri(@Nonnull UserId caller,
                                                        @Nonnull ProjectId projectId,
                                                        @Nonnull OWLEntity entity,
                                                        @Nonnull IRI newIri,
                                                        @Nullable String commitMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        if (entity.getIRI().equals(newIri)) {
            throw WpException.invalidRequest("The new IRI is the entity's IRI");
        }
        if (FreshEntityIri.isFreshEntityIri(newIri)) {
            throw WpException.invalidRequest("The IRI scheme entity: is reserved");
        }
        var renamed = context.dataFactory().getOWLEntity(entity.getEntityType(), newIri);
        requireNoBuiltInEntity(Set.of(entity, renamed));
        var before = ProjectContexts.read(context, ctx -> ctx.rendering().getRendering(entity));
        var generator = GuardedChange.of(new RenameEntityGenerator(context, entity, renamed))
                                     .withCommitMessage(commitMessage);
        var outcome = ProjectContexts.apply(context, caller, generator);
        if (outcome.revision().isPresent()) {
            discussionThreads.replaceEntity(projectId, entity, renamed);
        }
        return outcome.map(result -> new RenamedEntity(before, ProjectContexts.read(
                context, ctx -> ctx.rendering().getRendering(renamed))));
    }

    /**
     * The entities of the types whose names contain the words of the search string, for completing entity names
     * (legacy {@code LookupEntities}): each with the name that matched and where. The project's display name
     * languages are searched.
     *
     * @param limit    the most entities wanted, at most 100
     * @param criteria what the entities must also match, if anything
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public List<SearchResultMatch> lookupEntities(@Nonnull UserId caller,
                                                  @Nonnull ProjectId projectId,
                                                  @Nonnull String searchString,
                                                  @Nonnull Set<EntityType<?>> types,
                                                  int limit,
                                                  @Nullable EntityMatchCriteria criteria) {
        var size = Math.max(1, Math.min(limit, MAX_LOOKUP_RESULTS));
        return projects.read(caller, projectId, VIEW_PROJECT, context -> RegexBudget.limited(() -> {
            if (searchString.isBlank()) {
                return List.<SearchResultMatch>of();
            }
            var matcher = criteria != null ? context.matcherFactory().getMatcher(criteria) : null;
            var matches = context.dictionary()
                                 .getShortFormsContaining(SearchString.parseMultiWordSearchString(searchString),
                                                          types,
                                                          context.languageManager().getLanguages(),
                                                          ImmutableList.of(),
                                                          PageRequest.requestPageWithSize(1, size));
            var renderer = context.entityNodeRenderer();
            var seen = new HashSet<OWLEntity>();
            var results = new ArrayList<SearchResultMatch>();
            for (var match : matches.getPageElements()) {
                var entity = match.getEntity();
                if (match.getShortFormMatches().isEmpty() || !seen.add(entity)
                        || (matcher != null && !matcher.matches(entity))) {
                    continue;
                }
                var shortFormMatch = match.getShortFormMatches().get(0);
                var positions = shortFormMatch.getMatchPositions()
                                              .stream()
                                              .map(p -> SearchResultMatchPosition.get(p.getStart(), p.getEnd()))
                                              .collect(ImmutableList.toImmutableList());
                results.add(SearchResultMatch.get(renderer.render(entity),
                                                  shortFormMatch.getLanguage(),
                                                  ImmutableMap.of(),
                                                  shortFormMatch.getShortForm(),
                                                  positions));
            }
            return results;
        }));
    }

    /**
     * The deprecated entities of the types, a page at a time.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public Page<OWLEntityData> getDeprecatedEntities(@Nonnull UserId caller,
                                                     @Nonnull ProjectId projectId,
                                                     @Nonnull Set<EntityType<?>> types,
                                                     @Nonnull PageRequest pageRequest) {
        return projects.read(caller, projectId, VIEW_PROJECT,
                             context -> context.indexes()
                                               .get(DeprecatedEntitiesIndex.class)
                                               .getDeprecatedEntities(types, pageRequest)
                                               .transform(context.rendering()::getRendering));
    }

    /**
     * Where the entity is used: the axioms that mention it, as references sorted the legacy way, a page at a time.
     * The legacy handler cut the references at the page size before sorting them, so the first page was not the
     * first of the sorted references.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public EntityUsage getUsage(@Nonnull UserId caller,
                                @Nonnull ProjectId projectId,
                                @Nonnull OWLEntity subject,
                                @Nonnull UsageFilter filter,
                                @Nonnull PageRequest pageRequest) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var visitor = context.components().referencingAxiomVisitorFactory().create(subject);
            var axiomsByReference = context.indexes().get(AxiomsByReferenceIndex.class);
            var referencingAxioms = context.indexes()
                                           .get(ProjectOntologiesIndex.class)
                                           .getOntologyIds()
                                           .flatMap(ontologyId -> axiomsByReference.getReferencingAxioms(
                                                   Set.of(subject), ontologyId))
                                           .toList();
            var references = referencingAxioms.stream()
                                              .filter(axiom -> filter.isIncluded(axiom.getAxiomType()))
                                              .flatMap(axiom -> axiom.accept(visitor).stream())
                                              .filter(reference -> isIncludedBySubject(filter, subject, reference))
                                              .sorted(new UsageReferenceComparator(subject))
                                              .collect(PageCollector.toPage(pageRequest.getPageNumber(),
                                                                            pageRequest.getPageSize()))
                                              .orElse(Page.emptyPage());
            return new EntityUsage(context.entityNodeRenderer().render(subject), references,
                                   referencingAxioms.size());
        });
    }

    private static boolean isIncludedBySubject(UsageFilter filter, OWLEntity subject, UsageReference reference) {
        if (reference.getAxiomSubject().isEmpty()) {
            return true;
        }
        var axiomSubject = reference.getAxiomSubject().get();
        if (!filter.isIncluded(axiomSubject.getEntityType())) {
            return false;
        }
        return filter.isShowDefiningAxioms() || !subject.equals(axiomSubject);
    }

    private static <R> ChangeOutcome<R> apply(ProjectContext context,
                                              UserId caller,
                                              ChangeListGenerator<R> generator,
                                              @Nullable String commitMessage) {
        return ProjectContexts.apply(context, caller, GuardedChange.of(generator).withCommitMessage(commitMessage));
    }

    private static List<EntityNode> renderNodes(ProjectContext context, Set<? extends OWLEntity> entities) {
        var renderer = context.entityNodeRenderer();
        return entities.stream()
                       .map(renderer::render)
                       .sorted(Comparator.comparing(EntityNode::getBrowserText, String.CASE_INSENSITIVE_ORDER))
                       .toList();
    }

    private static <E extends OWLEntity> ImmutableSet<E> cast(Set<? extends OWLEntity> entities, Class<E> type) {
        return entities.stream().map(type::cast).collect(ImmutableSet.toImmutableSet());
    }

    private static void requireNoBuiltInEntity(Set<OWLEntity> entities) {
        entities.stream()
                .filter(OWLEntity::isBuiltIn)
                .findFirst()
                .ifPresent(entity -> {
                    throw WpException.invalidRequest("Built-in entities such as " + entity.getIRI()
                                                             + " cannot be changed");
                });
    }

    private static BuiltInAction createAction(EntityType<?> type) {
        if (type.equals(EntityType.CLASS)) {
            return CREATE_CLASS;
        } else if (type.equals(EntityType.NAMED_INDIVIDUAL)) {
            return CREATE_INDIVIDUAL;
        } else if (type.equals(EntityType.OBJECT_PROPERTY) || type.equals(EntityType.DATA_PROPERTY)
                || type.equals(EntityType.ANNOTATION_PROPERTY)) {
            return CREATE_PROPERTY;
        }
        throw WpException.invalidRequest("Entities of type " + type.getName() + " cannot be created here");
    }

    private static BuiltInAction deleteAction(EntityType<?> type) {
        if (type.equals(EntityType.CLASS)) {
            return DELETE_CLASS;
        } else if (type.equals(EntityType.NAMED_INDIVIDUAL)) {
            return DELETE_INDIVIDUAL;
        } else if (type.equals(EntityType.DATATYPE)) {
            return DELETE_DATATYPE;
        }
        return DELETE_PROPERTY;
    }

    /**
     * An entity before and after it got a new IRI.
     */
    public record RenamedEntity(@Nonnull OWLEntityData before, @Nonnull OWLEntityData after) {
    }

    /**
     * Where an entity is used.
     *
     * @param axiomCount how many axioms mention the entity, whatever the filter
     */
    public record EntityUsage(@Nonnull EntityNode subject, @Nonnull Page<UsageReference> references, int axiomCount) {
    }

    /**
     * Renames the entity in the changes it generates, which run when no other change can be applied, so the checks
     * hold until the renaming is applied.
     */
    private static final class RenameEntityGenerator implements ChangeListGenerator<OWLEntity> {

        private final ProjectContext context;

        private final OWLEntity entity;

        private final OWLEntity renamed;

        private RenameEntityGenerator(ProjectContext context, OWLEntity entity, OWLEntity renamed) {
            this.context = context;
            this.entity = entity;
            this.renamed = renamed;
        }

        @Override
        public OntologyChangeList<OWLEntity> generateChanges(ChangeGenerationContext generationContext) {
            var indexes = context.indexes();
            if (!indexes.get(EntitiesInProjectSignatureIndex.class).containsEntityInSignature(entity)) {
                throw WpException.notFound(ENTITY_NOT_FOUND, "The project has no " + entity.getEntityType()
                                                                                         .getPrintName()
                        .toLowerCase() + " " + entity.getIRI());
            }
            var inUse = indexes.get(EntitiesInProjectSignatureByIriIndex.class)
                               .getEntitiesInSignature(renamed.getIRI())
                               .anyMatch(existing -> existing.getEntityType().equals(entity.getEntityType()));
            if (inUse) {
                throw new WpException(IRI_IN_USE, 409, "The project already has a " + entity.getEntityType()
                                                                                          .getPrintName()
                        .toLowerCase() + " " + renamed.getIRI() + "; merge the two instead");
            }
            var changes = context.components().entityRenamer().generateChanges(ImmutableMap.of(entity,
                                                                                               renamed.getIRI()));
            return OntologyChangeList.<OWLEntity>builder().addAll(changes).build(renamed);
        }

        @Override
        public OWLEntity getRenamedResult(OWLEntity result, RenameMap renameMap) {
            return result;
        }

        @Nonnull
        @Override
        public String getMessage(ChangeApplicationResult<OWLEntity> result) {
            return String.format("Changed %s IRI from %s to %s",
                                 entity.getEntityType().getPrintName(),
                                 entity.getIRI(),
                                 renamed.getIRI());
        }
    }
}
