package org.industrial.ontology.kernel.change;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.EntityShortForm;
import org.industrial.ontology.domain.entity.FreshEntityIri;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.change.ProjectChangedKernelEvent;
import org.industrial.ontology.kernel.api.index.RootIndex;
import org.industrial.ontology.kernel.api.lang.ActiveLanguagesManager;
import org.industrial.ontology.kernel.api.port.ChangePermissionChecker;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.project.BuiltInPrefixDeclarations;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.industrial.ontology.kernel.api.util.IriReplacer;
import org.industrial.ontology.kernel.api.util.IriReplacerFactory;
import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;
import org.industrial.ontology.kernel.crud.EntityCrudContext;
import org.industrial.ontology.kernel.crud.EntityCrudContextFactory;
import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.PrefixedNameExpander;
import org.industrial.ontology.kernel.crud.ProjectEntityCrudKitHandlerCache;
import org.industrial.ontology.kernel.event.EventTranslatorManager;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.hierarchy.AnnotationPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.DataPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyHierarchyProvider;
import org.industrial.ontology.kernel.index.IndexUpdater;
import org.industrial.ontology.kernel.owlapi.OWLEntityCreator;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.owlapi.RenameMapFactory;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.kernel.shortform.DictionaryUpdatesProcessor;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.chg.ChangeManager}.
 * <p>
 * The only way axioms are written to a project (docs/00 §7 rule 4). Differences from the legacy class:
 * <ul>
 *     <li>Callers are already authorised: the application service requires {@code EDIT_ONTOLOGY} before calling
 *     {@link #applyChanges}. Creating fresh entities is checked through {@link ChangePermissionChecker} because the
 *     minted entity types are only known after generation (docs/01 §3.1, §5.1).</li>
 *     <li>The write lock is the project-wide {@link ReadWriteLock} owned by {@code ProjectContext}, shared with
 *     read services (docs/01 §3.4).</li>
 *     <li>Updating {@code ProjectDetails.modifiedAt} and invoking webhooks are replaced by publishing a
 *     {@link ProjectChangedKernelEvent} to listeners registered by wp-app.</li>
 * </ul>
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 20 Jun 2017
 */
public class ChangeManager implements HasApplyChanges {

    private static final Logger logger = LoggerFactory.getLogger(ChangeManager.class);

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final DictionaryUpdatesProcessor dictionaryUpdatesProcessor;

    @Nonnull
    private final ActiveLanguagesManager activeLanguagesManager;

    @Nonnull
    private final ChangePermissionChecker permissionChecker;

    @Nonnull
    private final PrefixDeclarationsStore prefixDeclarationsStore;

    @Nonnull
    private final ProjectEventManager projectEventManager;

    @Nonnull
    private final Supplier<EventTranslatorManager> eventTranslatorManagerProvider;

    @Nonnull
    private final ProjectEntityCrudKitHandlerCache entityCrudKitHandlerCache;

    @Nonnull
    private final RevisionManager changeManager;

    @Nonnull
    private final RootIndex rootIndex;

    @Nonnull
    private final DictionaryManager dictionaryManager;

    @Nonnull
    private final ClassHierarchyProvider classHierarchyProvider;

    @Nonnull
    private final ObjectPropertyHierarchyProvider objectPropertyHierarchyProvider;

    @Nonnull
    private final DataPropertyHierarchyProvider dataPropertyHierarchyProvider;

    @Nonnull
    private final AnnotationPropertyHierarchyProvider annotationPropertyHierarchyProvider;

    @Nonnull
    private final EntityCrudContextFactory entityCrudContextFactory;

    @Nonnull
    private final Lock projectChangeWriteLock;

    @Nonnull
    private final Lock changeProcesssingLock = new ReentrantLock();

    @Nonnull
    private final RenameMapFactory renameMapFactory;

    @Nonnull
    private final BuiltInPrefixDeclarations builtInPrefixDeclarations;

    @Nonnull
    private final IndexUpdater indexUpdater;

    @Nonnull
    private final DefaultOntologyIdManager defaultOntologyIdManager;

    @Nonnull
    private final IriReplacerFactory iriReplacerFactory;

    private final List<Consumer<ProjectChangedKernelEvent>> projectChangedListeners = new CopyOnWriteArrayList<>();

    public ChangeManager(@Nonnull ProjectId projectId,
                         @Nonnull ReadWriteLock projectLock,
                         @Nonnull OWLDataFactory dataFactory,
                         @Nonnull DictionaryUpdatesProcessor dictionaryUpdatesProcessor,
                         @Nonnull ActiveLanguagesManager activeLanguagesManager,
                         @Nonnull ChangePermissionChecker permissionChecker,
                         @Nonnull PrefixDeclarationsStore prefixDeclarationsStore,
                         @Nonnull ProjectEventManager projectEventManager,
                         @Nonnull Supplier<EventTranslatorManager> eventTranslatorManagerProvider,
                         @Nonnull ProjectEntityCrudKitHandlerCache entityCrudKitHandlerCache,
                         @Nonnull RevisionManager changeManager,
                         @Nonnull RootIndex rootIndex,
                         @Nonnull DictionaryManager dictionaryManager,
                         @Nonnull ClassHierarchyProvider classHierarchyProvider,
                         @Nonnull ObjectPropertyHierarchyProvider objectPropertyHierarchyProvider,
                         @Nonnull DataPropertyHierarchyProvider dataPropertyHierarchyProvider,
                         @Nonnull AnnotationPropertyHierarchyProvider annotationPropertyHierarchyProvider,
                         @Nonnull EntityCrudContextFactory entityCrudContextFactory,
                         @Nonnull RenameMapFactory renameMapFactory,
                         @Nonnull BuiltInPrefixDeclarations builtInPrefixDeclarations,
                         @Nonnull IndexUpdater indexUpdater,
                         @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                         @Nonnull IriReplacerFactory iriReplacerFactory) {
        this.projectId = checkNotNull(projectId);
        this.projectChangeWriteLock = checkNotNull(projectLock).writeLock();
        this.dataFactory = checkNotNull(dataFactory);
        this.dictionaryUpdatesProcessor = checkNotNull(dictionaryUpdatesProcessor);
        this.activeLanguagesManager = checkNotNull(activeLanguagesManager);
        this.permissionChecker = checkNotNull(permissionChecker);
        this.prefixDeclarationsStore = checkNotNull(prefixDeclarationsStore);
        this.projectEventManager = checkNotNull(projectEventManager);
        this.eventTranslatorManagerProvider = checkNotNull(eventTranslatorManagerProvider);
        this.entityCrudKitHandlerCache = checkNotNull(entityCrudKitHandlerCache);
        this.changeManager = checkNotNull(changeManager);
        this.rootIndex = checkNotNull(rootIndex);
        this.dictionaryManager = checkNotNull(dictionaryManager);
        this.classHierarchyProvider = checkNotNull(classHierarchyProvider);
        this.objectPropertyHierarchyProvider = checkNotNull(objectPropertyHierarchyProvider);
        this.dataPropertyHierarchyProvider = checkNotNull(dataPropertyHierarchyProvider);
        this.annotationPropertyHierarchyProvider = checkNotNull(annotationPropertyHierarchyProvider);
        this.entityCrudContextFactory = checkNotNull(entityCrudContextFactory);
        this.renameMapFactory = checkNotNull(renameMapFactory);
        this.builtInPrefixDeclarations = checkNotNull(builtInPrefixDeclarations);
        this.indexUpdater = checkNotNull(indexUpdater);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
        this.iriReplacerFactory = checkNotNull(iriReplacerFactory);
    }

    /**
     * Registers a listener that is called, outside the project write lock, after each committed revision.
     *
     * @return a handle that unregisters the listener
     */
    public AutoCloseable addProjectChangedListener(@Nonnull Consumer<ProjectChangedKernelEvent> listener) {
        projectChangedListeners.add(checkNotNull(listener));
        return () -> projectChangedListeners.remove(listener);
    }

    /**
     * Applies ontology changes to the ontologies contained within a project.
     *
     * @param userId              The userId of the user applying the changes.  Not {@code null}.
     * @param changeListGenerator A generator which creates a list of changes (based on the state of the project at
     *                            the time of change application).  The idea behind passing in a change generator is
     *                            that the list of changes to be applied can be created based on the state of the
     *                            project immediately before they are applied.  This is necessary where the changes
     *                            depend on the structure/state of the ontology.  This method guarantees that no third
     *                            party ontology changes will take place between the
     *                            {@link ChangeListGenerator#generateChanges(ChangeGenerationContext)}
     *                            method being called and the changes being applied.
     * @return A {@link ChangeApplicationResult} that describes the changes which took place an any renaminings.
     * @throws NullPointerException if any parameters are {@code null}.
     * @throws RuntimeException     from {@link ChangePermissionChecker} if the user may not create the fresh
     *                              entities that the changes mint.
     */
    @Override
    public <R> ChangeApplicationResult<R> applyChanges(@Nonnull final UserId userId,
                                                       @Nonnull final ChangeListGenerator<R> changeListGenerator) {
        checkNotNull(userId);
        checkNotNull(changeListGenerator);
        final ChangeApplicationResult<R> changeApplicationResult;
        var crudContext = getEntityCrudContext(userId);
        // The following must take into consideration fresh entity IRIs.  Entity IRIs are minted on the server, so
        // ontology changes may contain fresh entity IRIs as place holders. We need to make sure these get replaced
        // with true entity IRIs
        final Optional<Revision> revision;
        try {
            // Compute the changes that need to take place.  We don't allow any other writes here because the
            // generation of the changes may depend upon the state of the project
            changeProcesssingLock.lock();
            var changeList = changeListGenerator.generateChanges(new ChangeGenerationContext(userId));
            // We have our changes
            var changes = changeList.getChanges();
            // We coin fresh IRIs for entities that have IRIs that follow the temp IRI pattern
            // See DataFactory#isFreshEntity
            var tempIri2MintedIri = new HashMap<IRI, IRI>();
            var changeSession = getEntityCrudKitHandler().createChangeSetSession();
            // Changes that refer to entities that have temp IRIs
            var changesToBeRenamed = new HashSet<OntologyChange>();
            // Changes required to create fresh entities
            var changesToCreateFreshEntities = new ArrayList<OntologyChange>();
            for (var change : changes) {
                change.getSignature().forEach(entityInSignature -> {
                    if (isFreshEntity(entityInSignature)) {
                        permissionChecker.checkCreatePermission(userId, entityInSignature.getEntityType());
                        changesToBeRenamed.add(change);
                        var tempIri = entityInSignature.getIRI();
                        if (!tempIri2MintedIri.containsKey(tempIri)) {
                            var freshEntityIri = FreshEntityIri.parse(tempIri.toString());
                            var shortName = freshEntityIri.getSuppliedName();
                            var langTag = Optional.<String>empty();
                            if (!shortName.isEmpty()) {
                                langTag = Optional.of(freshEntityIri.getLangTag());
                            }
                            var entityType = entityInSignature.getEntityType();
                            var discriminator = freshEntityIri.getDiscriminator();
                            var parents = freshEntityIri.getParentEntities(dataFactory, entityType);
                            var creator = getEntityCreator(changeSession, crudContext, shortName, discriminator,
                                                           langTag, parents, entityType);
                            changesToCreateFreshEntities.addAll(creator.getChanges());
                            var mintedIri = creator.getEntity().getIRI();
                            tempIri2MintedIri.put(tempIri, mintedIri);
                        }
                    }
                });
                if (isChangeForAnnotationAssertionWithFreshIris(change)) {
                    changesToBeRenamed.add(change);
                }
            }
            var allChangesIncludingRenames = new ArrayList<OntologyChange>();
            var changeRenamer = iriReplacerFactory.create(ImmutableMap.copyOf(tempIri2MintedIri));
            for (var change : changes) {
                if (changesToBeRenamed.contains(change)) {
                    var replacementChange = getRenamedChange(change, changeRenamer);
                    allChangesIncludingRenames.add(replacementChange);
                } else {
                    allChangesIncludingRenames.add(change);
                }
            }
            allChangesIncludingRenames.addAll(changesToCreateFreshEntities);
            final var eventTranslatorManager = eventTranslatorManagerProvider.get();
            // Now we do the actual changing, so we lock the project here.  No writes or reads can take place whilst
            // we apply the changes
            projectChangeWriteLock.lock();
            try {
                var effectiveChanges = rootIndex.getEffectiveChanges(allChangesIncludingRenames);
                eventTranslatorManager.prepareForOntologyChanges(effectiveChanges);
                var renameMap = renameMapFactory.create(tempIri2MintedIri);
                var renamedResult = getRenamedResult(changeListGenerator, changeList.getResult(), renameMap);
                changeApplicationResult = new ChangeApplicationResult<>(renamedResult, effectiveChanges, renameMap);
                if (!effectiveChanges.isEmpty()) {
                    revision = Optional.of(logAndProcessAppliedChanges(userId, changeListGenerator,
                                                                       changeApplicationResult));
                } else {
                    revision = Optional.empty();
                }
            } finally {
                // Release for reads
                projectChangeWriteLock.unlock();
            }
            generateAndDispatchHighLevelEvents(changeListGenerator, changeApplicationResult, eventTranslatorManager,
                                               revision);
        } finally {
            changeProcesssingLock.unlock();
        }
        revision.ifPresent(rev -> notifyProjectChanged(userId, rev));
        return changeApplicationResult;
    }

    private void notifyProjectChanged(UserId userId, Revision revision) {
        var event = new ProjectChangedKernelEvent(projectId, userId, revision.getRevisionNumber(),
                                                  revision.getTimestamp());
        for (var listener : projectChangedListeners) {
            try {
                listener.accept(event);
            } catch (RuntimeException e) {
                logger.error("{} ProjectChangedKernelEvent listener failed for revision {}", projectId,
                             revision.getRevisionNumber(), e);
            }
        }
    }

    private EntityCrudContext getEntityCrudContext(UserId userId) {
        var prefixNameExpanderBuilder = PrefixedNameExpander.builder();
        prefixDeclarationsStore.find(projectId).getPrefixes().forEach(prefixNameExpanderBuilder::withPrefixNamePrefix);
        builtInPrefixDeclarations.getPrefixDeclarations()
                                 .forEach(decl -> prefixNameExpanderBuilder.withPrefixNamePrefix(decl.getPrefixName(),
                                                                                                 decl.getPrefix()));
        var prefixNameExpander = prefixNameExpanderBuilder.build();
        var defaultOntologyId = defaultOntologyIdManager.getDefaultOntologyId();
        return entityCrudContextFactory.create(userId, prefixNameExpander, defaultOntologyId);
    }

    @SuppressWarnings("unchecked")
    private <S extends EntityCrudKitSuffixSettings, C extends ChangeSetEntityCrudSession> EntityCrudKitHandler<S, C> getEntityCrudKitHandler() {
        return (EntityCrudKitHandler<S, C>) entityCrudKitHandlerCache.getHandler();
    }

    private static boolean isFreshEntity(OWLEntity entity) {
        return FreshEntityIri.isFreshEntityIri(entity.getIRI());
    }

    private <E extends OWLEntity> OWLEntityCreator<E> getEntityCreator(ChangeSetEntityCrudSession session,
                                                                       EntityCrudContext context,
                                                                       String shortName,
                                                                       String discriminator,
                                                                       Optional<String> langTag,
                                                                       ImmutableList<OWLEntity> parents,
                                                                       EntityType<E> entityType) {
        if (discriminator.isEmpty() && !shortName.isEmpty()) {
            Optional<E> entity = getEntityOfTypeIfPresent(entityType, shortName);
            if (entity.isPresent()) {
                return new OWLEntityCreator<>(entity.get(), Collections.emptyList());
            }
        }
        OntologyChangeList.Builder<E> builder = OntologyChangeList.builder();
        EntityCrudKitHandler<EntityCrudKitSuffixSettings, ChangeSetEntityCrudSession> handler = getEntityCrudKitHandler();
        handler.createChangeSetSession();
        E ent = handler.create(session, entityType, EntityShortForm.get(shortName), langTag, parents, context, builder);
        return new OWLEntityCreator<>(ent, builder.build(ent).getChanges());
    }

    private boolean isChangeForAnnotationAssertionWithFreshIris(OntologyChange change) {
        if (!change.isAxiomChange()) {
            return false;
        }
        var axiom = change.getAxiomOrThrow();
        if (!(axiom instanceof OWLAnnotationAssertionAxiom)) {
            return false;
        }
        var assertion = (OWLAnnotationAssertionAxiom) axiom;
        var subject = assertion.getSubject();
        if (subject instanceof IRI) {
            if (DataFactory.isFreshIri((IRI) subject)) {
                return true;
            }
        }
        var object = assertion.getValue();
        if (object instanceof IRI) {
            return DataFactory.isFreshIri((IRI) object);
        }
        return false;
    }

    /**
     * Gets an ontology change which is a copy of an existing ontology change except for IRIs that are renamed.
     *
     * @param change      The change to copy.
     * @param iriReplacer An IRI replacer used to rename IRIs in OWL objects
     * @return The ontology change with the renamings.
     */
    private OntologyChange getRenamedChange(OntologyChange change, IriReplacer iriReplacer) {
        return change.replaceIris(iriReplacer);
    }

    /**
     * Renames a result if it is present.
     *
     * @param result    The result to process.
     * @param renameMap The rename map.
     * @param <R>       The type of result.
     * @return The renamed (or untouched if no rename was necessary) result.
     */
    private <R> R getRenamedResult(ChangeListGenerator<R> changeListGenerator, R result, RenameMap renameMap) {
        return changeListGenerator.getRenamedResult(result, renameMap);
    }

    private <R> Revision logAndProcessAppliedChanges(UserId userId,
                                                     ChangeListGenerator<R> changeList,
                                                     ChangeApplicationResult<R> finalResult) {
        var changes = finalResult.getChangeList();
        // Update indexes in response to the changes
        indexUpdater.updateIndexes(ImmutableList.copyOf(changes));
        // Update the rendering first so that a proper change message is generated
        activeLanguagesManager.handleChanges(changes);
        dictionaryUpdatesProcessor.handleChanges(changes);
        // Generate a description for the changes that were actually applied
        var changeDescription = changeList.getMessage(finalResult);
        // Log the changes
        var revision = changeManager.addRevision(userId, changes, changeDescription);
        classHierarchyProvider.handleChanges(changes);
        objectPropertyHierarchyProvider.handleChanges(changes);
        dataPropertyHierarchyProvider.handleChanges(changes);
        annotationPropertyHierarchyProvider.handleChanges(changes);
        return revision;
    }

    private <R> void generateAndDispatchHighLevelEvents(ChangeListGenerator<R> changeListGenerator,
                                                        ChangeApplicationResult<R> finalResult,
                                                        EventTranslatorManager eventTranslatorManager,
                                                        Optional<Revision> revision) {
        if (changeListGenerator instanceof SilentChangeListGenerator) {
            return;
        }
        revision.ifPresent(rev -> {
            var highLevelEvents = new ArrayList<ProjectEvent>();
            eventTranslatorManager.translateOntologyChanges(rev, finalResult, highLevelEvents);
            if (changeListGenerator instanceof HasHighLevelEvents) {
                highLevelEvents.addAll(((HasHighLevelEvents) changeListGenerator).getHighLevelEvents());
            }
            projectEventManager.postEvents(highLevelEvents);
        });
    }

    @SuppressWarnings("unchecked")
    private <E extends OWLEntity> Optional<E> getEntityOfTypeIfPresent(EntityType<E> entityType, String shortName) {
        return dictionaryManager.getEntities(shortName)
                                .filter(entity -> entity.getEntityType().equals(entityType))
                                .map(entity -> (E) entity)
                                .findFirst();
    }
}
