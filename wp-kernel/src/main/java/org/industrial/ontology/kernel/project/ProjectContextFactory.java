package org.industrial.ontology.kernel.project;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.crud.oboid.OBOIdSuffixKit;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixKit;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixKit;
import org.industrial.ontology.kernel.api.entity.SubjectClosureResolver;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsByValueIndex;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByEntityReferenceIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.BuiltInOwlEntitiesIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectAnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.PropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.RootIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySuperPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.project.BuiltInPrefixDeclarations;
import org.industrial.ontology.kernel.api.shortform.LocalNameExtractor;
import org.industrial.ontology.kernel.api.util.IriReplacerFactory;
import org.industrial.ontology.kernel.change.ChangeManager;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslatorImpl;
import org.industrial.ontology.kernel.change.OntologyChangeSubjectProvider;
import org.industrial.ontology.kernel.crud.EntityCrudContextFactory;
import org.industrial.ontology.kernel.crud.EntityCrudKitPlugin;
import org.industrial.ontology.kernel.crud.EntityCrudKitPluginManager;
import org.industrial.ontology.kernel.crud.EntityCrudKitRegistry;
import org.industrial.ontology.kernel.crud.EntityIriPrefixCriteriaRewriter;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.kernel.crud.ProjectEntityCrudKitHandlerCache;
import org.industrial.ontology.kernel.crud.obo.OBOIdSuffixEntityCrudKitHandlerFactory;
import org.industrial.ontology.kernel.crud.obo.OBOIdSuffixEntityCrudKitPlugin;
import org.industrial.ontology.kernel.crud.supplied.SuppliedNameSuffixEntityCrudKitHandlerFactory;
import org.industrial.ontology.kernel.crud.supplied.SuppliedNameSuffixEntityCrudKitPlugin;
import org.industrial.ontology.kernel.crud.uuid.UuidEntityCrudKitHandlerFactory;
import org.industrial.ontology.kernel.crud.uuid.UuidEntityCrudKitPlugin;
import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.event.BrowserTextChangedEventComputer;
import org.industrial.ontology.kernel.event.EntityDeprecatedChangedEventTranslator;
import org.industrial.ontology.kernel.event.EntityTagsChangedEventComputer;
import org.industrial.ontology.kernel.event.EventTranslatorManager;
import org.industrial.ontology.kernel.event.HighLevelEventGenerator;
import org.industrial.ontology.kernel.event.OWLAnnotationPropertyHierarchyChangeComputer;
import org.industrial.ontology.kernel.event.OWLClassHierarchyChangeComputer;
import org.industrial.ontology.kernel.event.OWLDataPropertyHierarchyChangeComputer;
import org.industrial.ontology.kernel.event.OWLObjectPropertyHierarchyChangeComputer;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.frame.translator.Annotation2PropertyValueTranslator;
import org.industrial.ontology.kernel.frame.translator.AnnotationAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.AxiomPropertyValueTranslator;
import org.industrial.ontology.kernel.frame.translator.AxiomTranslatorFactory;
import org.industrial.ontology.kernel.frame.translator.ClassAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.ClassExpression2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.DataPropertyAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.EquivalentClassesAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.ObjectPropertyAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.SubClassOfAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.hierarchy.AnnotationPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyRootProvider;
import org.industrial.ontology.kernel.hierarchy.DataPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.DataPropertyHierarchyRootProvider;
import org.industrial.ontology.kernel.hierarchy.HierarchyProviders;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyHierarchyRootProvider;
import org.industrial.ontology.kernel.index.IndexUpdater;
import org.industrial.ontology.kernel.index.ProjectIndexes;
import org.industrial.ontology.kernel.lucene.DeprecatedEntitiesByEntityIndexLucene;
import org.industrial.ontology.kernel.lucene.DeprecatedEntitiesIndexLucene;
import org.industrial.ontology.kernel.lucene.DictionaryLanguageFieldWriter;
import org.industrial.ontology.kernel.lucene.EntityAnnotationAssertionsDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.EntityBasedSimilarity;
import org.industrial.ontology.kernel.lucene.EntityBuiltInStatusDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.EntityLocalNameDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.EntityOboIdDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.EntityPrefixedNameDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.EntitySearchFilterMatchersFactory;
import org.industrial.ontology.kernel.lucene.FieldNameTranslatorImpl;
import org.industrial.ontology.kernel.lucene.IndexingAnalyzerFactory;
import org.industrial.ontology.kernel.lucene.IndexingAnalyzerWrapper;
import org.industrial.ontology.kernel.lucene.LuceneDictionaryLanguageValuesMatcher;
import org.industrial.ontology.kernel.lucene.LuceneEntityDocumentTranslator;
import org.industrial.ontology.kernel.lucene.LuceneEntityDocumentTranslatorImpl;
import org.industrial.ontology.kernel.lucene.LuceneIndexImpl;
import org.industrial.ontology.kernel.lucene.LuceneIndexUpdaterImpl;
import org.industrial.ontology.kernel.lucene.LuceneIndexWriterImpl;
import org.industrial.ontology.kernel.lucene.LuceneModule;
import org.industrial.ontology.kernel.lucene.LuceneMultiLingualDictionaryUpdater;
import org.industrial.ontology.kernel.lucene.LuceneQueryFactory;
import org.industrial.ontology.kernel.lucene.LuceneSearchStringTokenizer;
import org.industrial.ontology.kernel.lucene.MultiLingualDictionaryLucene;
import org.industrial.ontology.kernel.lucene.MultiLingualShortFormDictionaryLucene;
import org.industrial.ontology.kernel.lucene.MultiLingualShortFormIndexLucene;
import org.industrial.ontology.kernel.lucene.QueryAnalyzerFactory;
import org.industrial.ontology.kernel.lucene.SearchFiltersDocumentAugmenter;
import org.industrial.ontology.kernel.lucene.SearchableMultiLingualShortFormDictionaryLucene;
import org.industrial.ontology.kernel.mansyntax.render.DefaultHttpLinkRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DeprecatedEntityCheckerImpl;
import org.industrial.ontology.kernel.mansyntax.render.EntityIRICheckerImpl;
import org.industrial.ontology.kernel.mansyntax.render.LiteralStyle;
import org.industrial.ontology.kernel.mansyntax.render.ManchesterSyntaxObjectRenderer;
import org.industrial.ontology.kernel.mansyntax.render.MarkdownLiteralRenderer;
import org.industrial.ontology.kernel.match.AnnotationValuesAreNotDisjointMatcherFactory;
import org.industrial.ontology.kernel.match.ConflictingBooleanValuesMatcherFactory;
import org.industrial.ontology.kernel.match.EntityAnnotationMatcherFactory;
import org.industrial.ontology.kernel.match.EntityIsDeprecatedMatcherFactory;
import org.industrial.ontology.kernel.match.EntityRelationshipMatcherFactory;
import org.industrial.ontology.kernel.match.InstanceOfMatcherFactory;
import org.industrial.ontology.kernel.match.IriAnnotationsMatcherFactory;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.kernel.match.MatchingEngine;
import org.industrial.ontology.kernel.match.NonUniqueLangTagsMatcherFactory;
import org.industrial.ontology.kernel.match.SubClassOfMatcherFactory;
import org.industrial.ontology.kernel.owlapi.HasContainsEntityInSignatureImpl;
import org.industrial.ontology.kernel.owlapi.RenameMapFactory;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.kernel.render.ShortFormAdapter;
import org.industrial.ontology.kernel.revision.RevisionManager;
import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.industrial.ontology.kernel.shortform.ActiveLanguagesManager;
import org.industrial.ontology.kernel.shortform.BuiltInShortFormDictionary;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.kernel.shortform.DictionaryUpdatesProcessor;
import org.industrial.ontology.kernel.shortform.LanguageManager;
import org.industrial.ontology.kernel.shortform.ShortFormCache;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds the {@link ProjectContext} of a project (docs/01 §2), replacing the Dagger provider methods of the legacy
 * {@code ProjectModule}, {@code IndexModule}, {@code ShortFormModule} and {@code LuceneModule}.
 * <p>
 * The construction order is the legacy one: revisions are loaded from the project directory, the indexes are built
 * by replaying them, then come the hierarchies, the Lucene dictionary (built in full when the project has no Lucene
 * index yet), rendering, events and finally the {@link ChangeManager}.
 * <p>
 * Components that the legacy code obtained from an unscoped {@code Provider} are created per use here as well: the
 * event translators hold the state of one change between {@code prepareForOntologyChanges} and
 * {@code translateOntologyChanges}, and each Lucene document translator reads the current search filters.
 * <p>
 * Thread pools are shared by all projects and never shut down by a project. If construction fails, everything
 * opened so far is released before the exception propagates.
 */
public class ProjectContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProjectContextFactory.class);

    private final DataDirectoryLayout dataDirectoryLayout;

    private final KernelThreadPools threadPools;

    private final ProjectPorts ports;

    private final BuiltInPrefixDeclarations builtInPrefixDeclarations;

    private final Duration eventRetention;

    /**
     * @param eventRetention how long project events stay readable with {@code getEventsFromTag}
     *                       ({@link ProjectEventManager#DEFAULT_RETENTION} by default, docs/01 §3.3)
     */
    public ProjectContextFactory(@Nonnull DataDirectoryLayout dataDirectoryLayout,
                                 @Nonnull KernelThreadPools threadPools,
                                 @Nonnull ProjectPorts ports,
                                 @Nonnull BuiltInPrefixDeclarations builtInPrefixDeclarations,
                                 @Nonnull Duration eventRetention) {
        this.dataDirectoryLayout = checkNotNull(dataDirectoryLayout);
        this.threadPools = checkNotNull(threadPools);
        this.ports = checkNotNull(ports);
        this.builtInPrefixDeclarations = checkNotNull(builtInPrefixDeclarations);
        this.eventRetention = checkNotNull(eventRetention);
    }

    /**
     * Loads the project from its directory, creating an empty project if it has no change history yet.
     */
    @Nonnull
    public ProjectContext create(@Nonnull ProjectId projectId) {
        checkNotNull(projectId);
        var stopwatch = Stopwatch.createStarted();
        var resources = new ProjectResources(projectId);
        try {
            var context = build(projectId, resources);
            logger.info("{} Loaded project in {} ms", projectId, stopwatch.elapsed().toMillis());
            return context;
        } catch(IOException e) {
            resources.releaseAll();
            throw new UncheckedIOException("Could not open the Lucene index of project " + projectId, e);
        } catch(RuntimeException | Error e) {
            resources.releaseAll();
            throw e;
        }
    }

    private ProjectContext build(@Nonnull ProjectId projectId,
                                 @Nonnull ProjectResources resources) throws IOException {
        OWLDataFactory dataFactory = new OWLDataFactoryImpl();

        // Revisions: the factory loads the change history, as the legacy RevisionStoreFactory did.
        var revisionStore = resources.add("revision store",
                                          new RevisionStoreFactory(dataDirectoryLayout.getChangeHistoryFileFactory(),
                                                                   dataFactory,
                                                                   new OntologyChangeRecordTranslatorImpl(),
                                                                   threadPools.revisionWrites())
                                                  .createRevisionStore(projectId),
                                          store -> store.dispose());
        var revisionManager = new RevisionManager(revisionStore);

        // Indexes, built by replaying the revisions
        var indexesBuilder = ProjectIndexes.builder(dataFactory);
        var indexUpdater = new IndexUpdater(revisionManager,
                                            indexesBuilder.updatable(),
                                            threadPools.indexUpdates(),
                                            projectId);
        indexUpdater.buildIndexes();
        // Marks the index as initialised when there was nothing to replay. Unlike the legacy provider, which ran
        // init before the replay and so counted every ontology twice, init here finds the replayed state and returns.
        indexesBuilder.get(org.industrial.ontology.kernel.index.ProjectOntologiesIndex.class).init(revisionManager);

        var projectOntologies = indexesBuilder.get(ProjectOntologiesIndex.class);
        var axiomsByType = indexesBuilder.get(AxiomsByTypeIndex.class);
        var axiomsByEntityReference = indexesBuilder.get(AxiomsByEntityReferenceIndex.class);
        var projectSignature = indexesBuilder.get(ProjectSignatureIndex.class);
        var projectSignatureByType = indexesBuilder.get(ProjectSignatureByTypeIndex.class);
        var entitiesInProjectSignature = indexesBuilder.get(EntitiesInProjectSignatureIndex.class);
        var entitiesInProjectSignatureByIri = indexesBuilder.get(EntitiesInProjectSignatureByIriIndex.class);
        var ontologySignatureByType = indexesBuilder.get(OntologySignatureByTypeIndex.class);
        var subClassOfAxioms = indexesBuilder.get(SubClassOfAxiomsBySubClassIndex.class);
        var equivalentClassesAxioms = indexesBuilder.get(EquivalentClassesAxiomsIndex.class);
        var annotationAssertionAxioms = indexesBuilder.get(AnnotationAssertionAxiomsIndex.class);
        var projectAnnotationAssertions = indexesBuilder.get(ProjectAnnotationAssertionAxiomsBySubjectIndex.class);

        // Hierarchies
        var classHierarchy = new ClassHierarchyProvider(projectId,
                                                        new ClassHierarchyRootProvider(dataFactory).get(),
                                                        projectOntologies,
                                                        subClassOfAxioms,
                                                        equivalentClassesAxioms,
                                                        projectSignatureByType,
                                                        axiomsByEntityReference,
                                                        entitiesInProjectSignatureByIri);
        var objectPropertyHierarchy = new ObjectPropertyHierarchyProvider(
                projectId,
                new ObjectPropertyHierarchyRootProvider(dataFactory).get(),
                entitiesInProjectSignature,
                projectOntologies,
                ontologySignatureByType,
                indexesBuilder.get(SubObjectPropertyAxiomsBySubPropertyIndex.class),
                axiomsByType);
        var dataPropertyHierarchy = new DataPropertyHierarchyProvider(
                projectId,
                new DataPropertyHierarchyRootProvider(dataFactory).get(),
                projectOntologies,
                axiomsByType,
                ontologySignatureByType,
                indexesBuilder.get(SubDataPropertyAxiomsBySubPropertyIndex.class),
                entitiesInProjectSignature);
        var annotationPropertyHierarchy = new AnnotationPropertyHierarchyProvider(
                projectId,
                dataFactory,
                projectSignatureByType,
                projectOntologies,
                indexesBuilder.get(SubAnnotationPropertyAxiomsBySubPropertyIndex.class),
                indexesBuilder.get(SubAnnotationPropertyAxiomsBySuperPropertyIndex.class),
                entitiesInProjectSignature);
        var hierarchies = new HierarchyProviders(classHierarchy,
                                                 objectPropertyHierarchy,
                                                 dataPropertyHierarchy,
                                                 annotationPropertyHierarchy);

        // Entity matching, used by search filters, CRUD kit prefixes and forms
        var classExpressionTranslator = new ClassExpression2PropertyValuesTranslator();
        var axiomPropertyValueTranslator = new AxiomPropertyValueTranslator(new AxiomTranslatorFactory(
                new SubClassOfAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new EquivalentClassesAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ClassAssertionAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ObjectPropertyAssertionAxiom2PropertyValuesTranslator(),
                new DataPropertyAssertionAxiom2PropertyValuesTranslator(),
                new AnnotationAssertionAxiom2PropertyValuesTranslator(new Annotation2PropertyValueTranslator())));
        Supplier<AnnotationAssertionAxiomsIndex> annotationAssertions = () -> annotationAssertionAxioms;
        var matcherFactory = new MatcherFactory(
                new SubClassOfMatcherFactory(() -> classHierarchy),
                new InstanceOfMatcherFactory(() -> classHierarchy,
                                             () -> projectOntologies,
                                             () -> indexesBuilder.get(ClassAssertionAxiomsByClassIndex.class),
                                             () -> projectSignatureByType),
                new ConflictingBooleanValuesMatcherFactory(annotationAssertions),
                new EntityIsDeprecatedMatcherFactory(annotationAssertions),
                new AnnotationValuesAreNotDisjointMatcherFactory(annotationAssertions),
                new NonUniqueLangTagsMatcherFactory(annotationAssertions),
                new EntityAnnotationMatcherFactory(annotationAssertions),
                new IriAnnotationsMatcherFactory(annotationAssertions),
                new EntityRelationshipMatcherFactory(() -> projectOntologies,
                                                     () -> subClassOfAxioms,
                                                     () -> indexesBuilder.get(
                                                             PropertyAssertionAxiomsBySubjectIndex.class),
                                                     () -> axiomPropertyValueTranslator));
        var matchingEngine = new MatchingEngine(projectSignature, matcherFactory);

        // Languages
        var projectDetailsRepository = ports.projectDetailsRepository();
        var activeLanguagesManager = new ActiveLanguagesManager(projectId, axiomsByEntityReference, projectOntologies);
        var languageManager = new LanguageManager(projectId, activeLanguagesManager, projectDetailsRepository);
        var builtInShortFormDictionary = new BuiltInShortFormDictionary(ShortFormCache.create(), dataFactory);
        builtInShortFormDictionary.load();

        // Lucene dictionary and search
        var fieldNameTranslator = new FieldNameTranslatorImpl();
        var fieldWriter = new DictionaryLanguageFieldWriter(fieldNameTranslator);
        var builtInStatusAugmenter = new EntityBuiltInStatusDocumentAugmenter();
        var localNameAugmenter = new EntityLocalNameDocumentAugmenter(new LocalNameExtractor(), fieldWriter);
        var prefixedNameAugmenter = new EntityPrefixedNameDocumentAugmenter(fieldWriter, builtInPrefixDeclarations);
        var oboIdAugmenter = new EntityOboIdDocumentAugmenter(fieldWriter);
        var annotationsAugmenter = new EntityAnnotationAssertionsDocumentAugmenter(projectAnnotationAssertions,
                                                                                   fieldWriter);
        var searchFilterMatchers = new EntitySearchFilterMatchersFactory(ports.entitySearchFiltersManager(projectId),
                                                                         matcherFactory);
        Supplier<LuceneEntityDocumentTranslator> documentTranslators = () -> new LuceneEntityDocumentTranslatorImpl(
                fieldNameTranslator,
                builtInStatusAugmenter,
                localNameAugmenter,
                prefixedNameAugmenter,
                oboIdAugmenter,
                annotationsAugmenter,
                new SearchFiltersDocumentAugmenter(searchFilterMatchers.getSearchFilterMatchers()),
                dataFactory);
        var indexingAnalyzerFactory = new IndexingAnalyzerFactory(
                () -> new IndexingAnalyzerWrapper(LuceneModule.MIN_GRAM_SIZE, LuceneModule.MAX_GRAM_SIZE));
        var directory = resources.add("Lucene directory",
                                      FSDirectory.open(dataDirectoryLayout.getLuceneDirectoryPathSupplier(projectId)
                                                                          .get()),
                                      FSDirectory::close);
        var indexWriter = resources.add("Lucene index writer",
                                        new IndexWriter(directory,
                                                        new IndexWriterConfig(indexingAnalyzerFactory.get())
                                                                .setSimilarity(new EntityBasedSimilarity())),
                                        IndexWriter::close);
        var searcherManager = resources.add("Lucene searcher manager",
                                            new SearcherManager(indexWriter, new SearcherFactory()),
                                            SearcherManager::close);
        var luceneIndexWriter = new LuceneIndexWriterImpl(projectId,
                                                          directory,
                                                          documentTranslators,
                                                          projectSignature,
                                                          entitiesInProjectSignature,
                                                          indexWriter,
                                                          searcherManager,
                                                          indexesBuilder.get(BuiltInOwlEntitiesIndex.class));
        luceneIndexWriter.writeIndex();
        var queryAnalyzerFactory = new QueryAnalyzerFactory();
        var luceneIndex = new LuceneIndexImpl(documentTranslators.get(),
                                              searcherManager,
                                              new LuceneQueryFactory(fieldNameTranslator, queryAnalyzerFactory),
                                              new LuceneDictionaryLanguageValuesMatcher(
                                                      new LuceneSearchStringTokenizer(queryAnalyzerFactory),
                                                      fieldNameTranslator,
                                                      indexingAnalyzerFactory),
                                              queryAnalyzerFactory);
        var dictionaryManager = new DictionaryManager(languageManager,
                                                      new MultiLingualDictionaryLucene(
                                                              new MultiLingualShortFormDictionaryLucene(luceneIndex),
                                                              new SearchableMultiLingualShortFormDictionaryLucene(
                                                                      luceneIndex),
                                                              new MultiLingualShortFormIndexLucene(luceneIndex)),
                                                      new LuceneMultiLingualDictionaryUpdater(
                                                              new LuceneIndexUpdaterImpl(indexWriter,
                                                                                         documentTranslators,
                                                                                         searcherManager)),
                                                      builtInShortFormDictionary);
        var deprecatedEntitiesByEntity = new DeprecatedEntitiesByEntityIndexLucene(searcherManager,
                                                                                   documentTranslators.get());
        var indexes = indexesBuilder.build(classHierarchy,
                                           dictionaryManager,
                                           new DeprecatedEntitiesIndexLucene(searcherManager,
                                                                             documentTranslators.get()),
                                           deprecatedEntitiesByEntity);

        // Rendering
        var deprecatedEntityChecker = new DeprecatedEntityCheckerImpl(deprecatedEntitiesByEntity);
        var renderingManager = new RenderingManager(dictionaryManager,
                                                    deprecatedEntityChecker,
                                                    new ManchesterSyntaxObjectRenderer(
                                                            new ShortFormAdapter(dictionaryManager),
                                                            new EntityIRICheckerImpl(entitiesInProjectSignatureByIri),
                                                            LiteralStyle.REGULAR,
                                                            new DefaultHttpLinkRenderer(),
                                                            new MarkdownLiteralRenderer()));

        // Events
        var eventManager = resources.add("event manager",
                                         new ProjectEventManager(projectId,
                                                                 eventRetention,
                                                                 ProjectEventManager.DEFAULT_MAX_BATCH_SIZE,
                                                                 threadPools.eventPurges()),
                                         ProjectEventManager::dispose);
        var changeSubjectProvider = new OntologyChangeSubjectProvider(
                entitiesInProjectSignatureByIri,
                new SubjectClosureResolver(indexes.get(AnnotationAssertionAxiomsByValueIndex.class),
                                           projectOntologies,
                                           entitiesInProjectSignatureByIri));
        var containsEntityInSignature = new HasContainsEntityInSignatureImpl(entitiesInProjectSignature);
        var tagsManager = ports.tagsManager(projectId, matchingEngine);
        var entityNodeRenderer = new EntityNodeRenderer(projectId,
                                                        dictionaryManager,
                                                        deprecatedEntityChecker,
                                                        ports.watchManager(projectId),
                                                        ports.entityDiscussionThreadRepository(),
                                                        tagsManager,
                                                        languageManager);
        var entityTagsChangedEventComputer = new EntityTagsChangedEventComputer(projectId,
                                                                                changeSubjectProvider,
                                                                                tagsManager);
        Supplier<EventTranslatorManager> eventTranslators = () -> new EventTranslatorManager(ImmutableSet.of(
                new BrowserTextChangedEventComputer(projectId,
                                                    dictionaryManager,
                                                    changeSubjectProvider,
                                                    containsEntityInSignature),
                new HighLevelEventGenerator(projectId,
                                            renderingManager,
                                            entitiesInProjectSignatureByIri,
                                            revisionManager,
                                            entitiesInProjectSignature),
                new OWLClassHierarchyChangeComputer(projectId, classHierarchy, entityNodeRenderer, classHierarchy),
                new OWLObjectPropertyHierarchyChangeComputer(projectId, objectPropertyHierarchy, entityNodeRenderer),
                new OWLDataPropertyHierarchyChangeComputer(projectId, dataPropertyHierarchy, entityNodeRenderer),
                new OWLAnnotationPropertyHierarchyChangeComputer(projectId,
                                                                 annotationPropertyHierarchy,
                                                                 entityNodeRenderer),
                new EntityDeprecatedChangedEventTranslator(projectId,
                                                           deprecatedEntityChecker,
                                                           entitiesInProjectSignatureByIri),
                entityTagsChangedEventComputer));

        // Entity CRUD kits
        var entityIriPrefixResolver = new EntityIriPrefixResolver(matcherFactory,
                                                                  new EntityIriPrefixCriteriaRewriter());
        Set<EntityCrudKitPlugin<?, ?, ?>> crudKitPlugins = Set.of(
                new UuidEntityCrudKitPlugin(new UuidSuffixKit(),
                                            new UuidEntityCrudKitHandlerFactory(() -> dataFactory,
                                                                                () -> entitiesInProjectSignatureByIri,
                                                                                () -> entityIriPrefixResolver)),
                new OBOIdSuffixEntityCrudKitPlugin(new OBOIdSuffixKit(),
                                                   new OBOIdSuffixEntityCrudKitHandlerFactory(
                                                           () -> dataFactory,
                                                           () -> entitiesInProjectSignatureByIri,
                                                           () -> entityIriPrefixResolver)),
                new SuppliedNameSuffixEntityCrudKitPlugin(new SuppliedNameSuffixKit(),
                                                          new SuppliedNameSuffixEntityCrudKitHandlerFactory(
                                                                  () -> dataFactory,
                                                                  () -> entityIriPrefixResolver)));
        var crudKitHandlerCache = new ProjectEntityCrudKitHandlerCache(
                ports.entityCrudKitSettingsRepository(),
                projectId,
                new EntityCrudKitRegistry(new EntityCrudKitPluginManager(crudKitPlugins)));

        // Changes
        var defaultOntologyIdManager = new DefaultOntologyIdManagerImpl(projectOntologies);
        var lock = new ReentrantReadWriteLock();
        var changeManager = new ChangeManager(projectId,
                                              lock,
                                              dataFactory,
                                              new DictionaryUpdatesProcessor(changeSubjectProvider,
                                                                             dictionaryManager),
                                              activeLanguagesManager,
                                              ports.changePermissionChecker(projectId),
                                              ports.prefixDeclarationsStore(),
                                              eventManager,
                                              eventTranslators,
                                              crudKitHandlerCache,
                                              revisionManager,
                                              indexes.get(RootIndex.class),
                                              dictionaryManager,
                                              classHierarchy,
                                              objectPropertyHierarchy,
                                              dataPropertyHierarchy,
                                              annotationPropertyHierarchy,
                                              new EntityCrudContextFactory(() -> projectId,
                                                                           () -> projectDetailsRepository),
                                              new RenameMapFactory(() -> dataFactory, () -> renderingManager),
                                              builtInPrefixDeclarations,
                                              indexUpdater,
                                              defaultOntologyIdManager,
                                              new IriReplacerFactory(() -> dataFactory));

        return new ProjectContext(projectId,
                                  dataFactory,
                                  indexes,
                                  indexUpdater,
                                  revisionStore,
                                  revisionManager,
                                  hierarchies,
                                  languageManager,
                                  activeLanguagesManager,
                                  dictionaryManager,
                                  luceneIndexWriter,
                                  deprecatedEntityChecker,
                                  renderingManager,
                                  entityNodeRenderer,
                                  matchingEngine,
                                  matcherFactory,
                                  defaultOntologyIdManager,
                                  crudKitHandlerCache,
                                  eventManager,
                                  changeManager,
                                  lock,
                                  resources,
                                  Instant.now());
    }
}
