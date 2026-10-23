package org.industrial.ontology.kernel.project;

import org.industrial.ontology.domain.frame.PropertyValue;
import org.industrial.ontology.domain.object.OWLClassExpressionSelector;
import org.industrial.ontology.domain.object.OWLDataPropertyExpressionSelector;
import org.industrial.ontology.domain.object.OWLIndividualSelector;
import org.industrial.ontology.domain.object.OWLObjectPropertyExpressionSelector;
import org.industrial.ontology.domain.object.SWRLAtomSelector;
import org.industrial.ontology.kernel.api.change.OntologyChangeComparator;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyCharacteristicsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DifferentIndividualsAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointDataPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointObjectPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EquivalentDataPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EquivalentObjectPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.InverseObjectPropertyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.NamedIndividualFrameAxiomIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyCharacteristicsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SameIndividualAxiomsIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.change.CreateAnnotationPropertiesChangeGeneratorFactory;
import org.industrial.ontology.kernel.change.CreateClassesChangeGeneratorFactory;
import org.industrial.ontology.kernel.change.CreateDataPropertiesChangeGeneratorFactory;
import org.industrial.ontology.kernel.change.CreateObjectPropertiesChangeGeneratorFactory;
import org.industrial.ontology.kernel.change.ReverseEngineeredChangeDescriptionGeneratorFactory;
import org.industrial.ontology.kernel.change.bulkop.EditAnnotationsChangeListGeneratorFactory;
import org.industrial.ontology.kernel.change.bulkop.MoveClassesChangeListGeneratorFactory;
import org.industrial.ontology.kernel.change.bulkop.SetAnnotationValueActionChangeListGeneratorFactory;
import org.industrial.ontology.kernel.change.matcher.AnnotationAssertionChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.ChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.ClassAssertionAxiomMatcher;
import org.industrial.ontology.kernel.change.matcher.ClassMoveChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.EditedAnnotationAssertionChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.EntityCreationMatcher;
import org.industrial.ontology.kernel.change.matcher.EntityDeletionMatcher;
import org.industrial.ontology.kernel.change.matcher.FunctionalDataPropertyAxiomChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.PropertyAssertionAxiomMatcher;
import org.industrial.ontology.kernel.change.matcher.PropertyDomainAxiomChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.PropertyRangeAxiomChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.SameIndividualAxiomChangeMatcher;
import org.industrial.ontology.kernel.change.matcher.SubClassOfAxiomMatcher;
import org.industrial.ontology.kernel.change.matcher.SubClassOfEditChangeMatcher;
import org.industrial.ontology.kernel.crud.DeleteEntitiesChangeListGeneratorFactory;
import org.industrial.ontology.kernel.entity.EntityRenamer;
import org.industrial.ontology.kernel.entity.MergeEntitiesChangeListGeneratorFactory;
import org.industrial.ontology.kernel.frame.ClassFrameProvider;
import org.industrial.ontology.kernel.frame.ClassFrameProviderImpl;
import org.industrial.ontology.kernel.frame.FrameChangeGenerator;
import org.industrial.ontology.kernel.frame.FrameChangeGeneratorFactory;
import org.industrial.ontology.kernel.frame.FrameComponentRenderer;
import org.industrial.ontology.kernel.frame.FrameComponentSessionRenderer;
import org.industrial.ontology.kernel.frame.FrameUpdate;
import org.industrial.ontology.kernel.frame.PropertyValueComparator;
import org.industrial.ontology.kernel.frame.PropertyValueMinimiser;
import org.industrial.ontology.kernel.frame.StructuralPropertyValueSubsumptionChecker;
import org.industrial.ontology.kernel.frame.translator.Annotation2PropertyValueTranslator;
import org.industrial.ontology.kernel.frame.translator.AnnotationAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.AnnotationPropertyFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.AxiomPropertyValueTranslator;
import org.industrial.ontology.kernel.frame.translator.AxiomTranslatorFactory;
import org.industrial.ontology.kernel.frame.translator.Class2ClassFrameTranslatorFactory;
import org.industrial.ontology.kernel.frame.translator.ClassAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.ClassExpression2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.ClassFrame2FrameAxiomsTranslator;
import org.industrial.ontology.kernel.frame.translator.DataPropertyAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.DataPropertyFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.EquivalentClassesAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.NamedIndividualFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.ObjectPropertyAssertionAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.frame.translator.ObjectPropertyFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.PropertyValue2AxiomTranslator;
import org.industrial.ontology.kernel.frame.translator.SubClassOfAxiom2PropertyValuesTranslator;
import org.industrial.ontology.kernel.hierarchy.ClassClassAncestorChecker;
import org.industrial.ontology.kernel.hierarchy.DataPropertyDataPropertyAncestorChecker;
import org.industrial.ontology.kernel.hierarchy.GraphNodeRenderer;
import org.industrial.ontology.kernel.hierarchy.HierarchyProviderMapper;
import org.industrial.ontology.kernel.hierarchy.MoveEntityChangeListGeneratorFactory;
import org.industrial.ontology.kernel.hierarchy.NamedIndividualClassAncestorChecker;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyObjectPropertyAncestorChecker;
import org.industrial.ontology.kernel.individuals.CreateIndividualsChangeListGeneratorFactory;
import org.industrial.ontology.kernel.mansyntax.ManchesterSyntaxChangeGeneratorFactory;
import org.industrial.ontology.kernel.mansyntax.ManchesterSyntaxFrameParser;
import org.industrial.ontology.kernel.mansyntax.OntologyAxiomPairChangeGenerator;
import org.industrial.ontology.kernel.mansyntax.ShellOntologyChecker;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationPropertyComparator;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationPropertyDomainSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationPropertyFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationPropertyRangeSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationPropertySubPropertyOfSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.AnnotationsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ClassDisjointWithSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ClassEquivalentToSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ClassFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ClassSubClassOfSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyCharacteristicsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyDisjointWithSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyDomainSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyEquivalentToSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertyRangeSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DataPropertySubPropertyOfSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DefaultHttpLinkRenderer;
import org.industrial.ontology.kernel.mansyntax.render.DefaultItemStyleProvider;
import org.industrial.ontology.kernel.mansyntax.render.EntityIRICheckerImpl;
import org.industrial.ontology.kernel.mansyntax.render.IRIOrdinalProvider;
import org.industrial.ontology.kernel.mansyntax.render.LiteralStyle;
import org.industrial.ontology.kernel.mansyntax.render.ManchesterSyntaxEntityFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ManchesterSyntaxObjectRenderer;
import org.industrial.ontology.kernel.mansyntax.render.MarkdownLiteralRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NamedIndividualDifferentFromSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NamedIndividualFactsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NamedIndividualFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NamedIndividualSameAsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NamedIndividualTypesSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.NestedAnnotationStyle;
import org.industrial.ontology.kernel.mansyntax.render.NullHighlightedEntityChecker;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyCharacteristicsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyDisjointWithSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyDomainSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyEquivalentToSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyFrameRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyInverseOfSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertyRangeSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertySubPropertyChainSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.ObjectPropertySubPropertyOfRenderer;
import org.industrial.ontology.kernel.mansyntax.render.OntologyAnnotationsSectionRenderer;
import org.industrial.ontology.kernel.mansyntax.render.OwlOntologyFacadeFactory;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.industrial.ontology.kernel.owlapi.StringFormatterLiteralRenderer;
import org.industrial.ontology.kernel.render.ContextRenderer;
import org.industrial.ontology.kernel.render.ShortFormAdapter;
import org.industrial.ontology.kernel.search.EntitySearcherFactory;
import org.industrial.ontology.kernel.shortform.EscapingShortFormProvider;
import org.industrial.ontology.kernel.shortform.IriShortFormAdapter;
import org.industrial.ontology.kernel.shortform.WebProtegeOntologyIRIShortFormProvider;
import org.industrial.ontology.kernel.revision.ProjectChangesManager;
import org.industrial.ontology.kernel.revision.RevisionDetailsExtractor;
import org.industrial.ontology.kernel.axiom.AxiomByRenderingComparator;
import org.industrial.ontology.kernel.axiom.AxiomBySubjectComparator;
import org.industrial.ontology.kernel.axiom.AxiomByTypeComparator;
import org.industrial.ontology.kernel.axiom.AxiomComparator;
import org.industrial.ontology.kernel.axiom.AxiomSubjectProvider;
import org.industrial.ontology.kernel.axiom.DefaultAxiomTypeOrdering;
import org.industrial.ontology.kernel.change.RevisionReverterChangeListGeneratorFactory;
import org.industrial.ontology.kernel.diff.Revision2DiffElementsTranslator;
import org.industrial.ontology.kernel.object.OWLObjectComparator;
import org.industrial.ontology.kernel.render.OWLObjectRendererImpl;
import org.industrial.ontology.kernel.usage.ReferencingAxiomVisitorFactory;
import org.industrial.ontology.kernel.util.EntityDeleter;
import org.industrial.ontology.kernel.util.ReferenceFinder;
import org.semanticweb.owlapi.util.ShortFormProvider;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The collaborators of the ontology editing and search use cases of one project (docs/01 §5.1, stage S7), wired from
 * its {@link ProjectContext} as the legacy {@code ProjectModule} bound them. wp-app's services get them from
 * {@link ProjectContext#components()}, which creates this object on first use.
 * <p>
 * What the legacy module bound as a project singleton is created once here. What it obtained from an unscoped
 * {@code Provider} is created by the {@code new...} methods on each call: the frame translators (the individual
 * translator has a minimisation switch), the session renderer (it caches renderings for one request), the Manchester
 * syntax parser and the HTML frame renderer.
 */
public final class ProjectComponents {

    /** The legacy {@code HasLang} binding: property values are ordered for English. */
    private static final String PROPERTY_VALUE_LANGUAGE = "en";

    private final ProjectContext context;

    private final MessageFormatter messageFormatter;

    private final ShortFormProvider shortFormProvider;

    private final WebProtegeOntologyIRIShortFormProvider ontologyIriShortFormProvider;

    private final ReverseEngineeredChangeDescriptionGeneratorFactory changeDescriptions;

    private final AxiomPropertyValueTranslator axiomPropertyValueTranslator;

    private final PropertyValueMinimiser propertyValueMinimiser;

    private final PropertyValue2AxiomTranslator propertyValue2AxiomTranslator;

    private final ClassFrameProvider classFrameProvider;

    private final FrameComponentRenderer frameComponentRenderer;

    private final Comparator<PropertyValue> propertyValueComparator;

    private final ContextRenderer contextRenderer;

    private final OwlOntologyFacadeFactory ontologyFacadeFactory;

    private final ManchesterSyntaxChangeGeneratorFactory manchesterSyntaxChangeGeneratorFactory;

    private final EntityRenamer entityRenamer;

    private final CreateClassesChangeGeneratorFactory createClasses;

    private final CreateObjectPropertiesChangeGeneratorFactory createObjectProperties;

    private final CreateDataPropertiesChangeGeneratorFactory createDataProperties;

    private final CreateAnnotationPropertiesChangeGeneratorFactory createAnnotationProperties;

    private final CreateIndividualsChangeListGeneratorFactory createIndividuals;

    private final DeleteEntitiesChangeListGeneratorFactory deleteEntities;

    private final MergeEntitiesChangeListGeneratorFactory mergeEntities;

    private final ReferencingAxiomVisitorFactory referencingAxiomVisitors;

    private final HierarchyProviderMapper hierarchyProviders;

    private final GraphNodeRenderer graphNodeRenderer;

    private final MoveEntityChangeListGeneratorFactory moveEntities;

    private final MoveClassesChangeListGeneratorFactory moveClasses;

    private final SetAnnotationValueActionChangeListGeneratorFactory setAnnotationValues;

    private final EditAnnotationsChangeListGeneratorFactory editAnnotations;

    private final EntitySearcherFactory entitySearchers;

    private final ProjectChangesManager projectChanges;

    private final RevisionDetailsExtractor revisionDetails;

    private final RevisionReverterChangeListGeneratorFactory revisionReverters;

    ProjectComponents(@Nonnull ProjectContext context,
                      @Nonnull EntityDiscussionThreadRepository discussionThreadRepository) {
        this.context = checkNotNull(context);
        checkNotNull(discussionThreadRepository);
        var projectId = context.projectId();
        var dataFactory = context.dataFactory();
        var rendering = context.rendering();
        var dictionary = context.dictionary();
        var hierarchies = context.hierarchies();
        var defaultOntologyIdManager = context.defaultOntologyIdManager();
        var projectOntologies = index(ProjectOntologiesIndex.class);
        var entitiesInSignatureByIri = index(EntitiesInProjectSignatureByIriIndex.class);
        var annotationAssertions = index(AnnotationAssertionAxiomsBySubjectIndex.class);
        messageFormatter = new MessageFormatter(rendering);
        shortFormProvider = new ShortFormAdapter(dictionary);
        ontologyIriShortFormProvider = new WebProtegeOntologyIRIShortFormProvider(defaultOntologyIdManager);
        changeDescriptions = newChangeDescriptions();
        contextRenderer = new ContextRenderer(rendering);
        // Frames
        var classExpressionTranslator = new ClassExpression2PropertyValuesTranslator();
        axiomPropertyValueTranslator = new AxiomPropertyValueTranslator(new AxiomTranslatorFactory(
                new SubClassOfAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new EquivalentClassesAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ClassAssertionAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ObjectPropertyAssertionAxiom2PropertyValuesTranslator(),
                new DataPropertyAssertionAxiom2PropertyValuesTranslator(),
                new AnnotationAssertionAxiom2PropertyValuesTranslator(new Annotation2PropertyValueTranslator())));
        var classAncestors = new ClassClassAncestorChecker(hierarchies.classHierarchy());
        propertyValueMinimiser = new PropertyValueMinimiser(new StructuralPropertyValueSubsumptionChecker(
                classAncestors,
                new ObjectPropertyObjectPropertyAncestorChecker(hierarchies.objectPropertyHierarchy()),
                new DataPropertyDataPropertyAncestorChecker(hierarchies.dataPropertyHierarchy()),
                new NamedIndividualClassAncestorChecker(classAncestors,
                                                        index(ClassAssertionAxiomsByIndividualIndex.class),
                                                        projectOntologies)));
        propertyValue2AxiomTranslator = new PropertyValue2AxiomTranslator();
        var matcherFactory = context.matcherFactory();
        classFrameProvider = new ClassFrameProviderImpl(new Class2ClassFrameTranslatorFactory(
                () -> index(ClassFrameAxiomsIndex.class),
                hierarchies::classHierarchy,
                () -> propertyValueMinimiser,
                () -> axiomPropertyValueTranslator,
                () -> matcherFactory));
        frameComponentRenderer = new FrameComponentRenderer(rendering, entitiesInSignatureByIri,
                                                            entitiesInSignatureByIri);
        propertyValueComparator = new PropertyValueComparator(
                new AnnotationPropertyComparator(shortFormProvider,
                                                 IRIOrdinalProvider.withDefaultAnnotationPropertyOrdering()),
                () -> PROPERTY_VALUE_LANGUAGE);
        // Manchester syntax
        ontologyFacadeFactory = newOntologyFacadeFactory();
        manchesterSyntaxChangeGeneratorFactory = new ManchesterSyntaxChangeGeneratorFactory(
                this::newManchesterSyntaxFrameParser,
                OntologyAxiomPairChangeGenerator::new,
                () -> changeDescriptions);
        // Entities
        entityRenamer = new EntityRenamer(dataFactory, projectOntologies, index(AxiomsByReferenceIndex.class));
        Supplier<MessageFormatter> messages = () -> messageFormatter;
        createClasses = new CreateClassesChangeGeneratorFactory(() -> dataFactory, messages,
                                                                () -> defaultOntologyIdManager);
        createObjectProperties = new CreateObjectPropertiesChangeGeneratorFactory(() -> dataFactory, messages,
                                                                                  () -> defaultOntologyIdManager);
        createDataProperties = new CreateDataPropertiesChangeGeneratorFactory(() -> dataFactory, messages,
                                                                              () -> defaultOntologyIdManager);
        createAnnotationProperties = new CreateAnnotationPropertiesChangeGeneratorFactory(
                () -> dataFactory, messages, () -> defaultOntologyIdManager);
        createIndividuals = new CreateIndividualsChangeListGeneratorFactory(() -> dataFactory, messages,
                                                                            () -> defaultOntologyIdManager);
        var entityDeleter = new EntityDeleter(new ReferenceFinder(index(AxiomsByReferenceIndex.class),
                                                                  index(OntologyAnnotationsIndex.class)),
                                              projectOntologies);
        deleteEntities = new DeleteEntitiesChangeListGeneratorFactory(messages, () -> entityDeleter);
        mergeEntities = new MergeEntitiesChangeListGeneratorFactory(() -> projectId,
                                                                    () -> dataFactory,
                                                                    () -> discussionThreadRepository,
                                                                    () -> entityRenamer,
                                                                    () -> defaultOntologyIdManager,
                                                                    () -> projectOntologies,
                                                                    () -> annotationAssertions);
        referencingAxiomVisitors = new ReferencingAxiomVisitorFactory(() -> rendering,
                                                                      () -> entitiesInSignatureByIri);
        // Hierarchies
        hierarchyProviders = new HierarchyProviderMapper(hierarchies.classHierarchy(),
                                                         hierarchies.objectPropertyHierarchy(),
                                                         hierarchies.dataPropertyHierarchy(),
                                                         hierarchies.annotationPropertyHierarchy());
        graphNodeRenderer = new GraphNodeRenderer(context.entityNodeRenderer());
        moveEntities = new MoveEntityChangeListGeneratorFactory(
                dataFactory,
                messageFormatter,
                projectOntologies,
                defaultOntologyIdManager,
                index(EquivalentClassesAxiomsIndex.class),
                index(SubClassOfAxiomsBySubClassIndex.class),
                index(SubObjectPropertyAxiomsBySubPropertyIndex.class),
                index(SubDataPropertyAxiomsBySubPropertyIndex.class),
                index(SubAnnotationPropertyAxiomsBySubPropertyIndex.class));
        // Bulk edits
        moveClasses = new MoveClassesChangeListGeneratorFactory(() -> projectOntologies,
                                                                () -> index(SubClassOfAxiomsBySubClassIndex.class),
                                                                () -> dataFactory);
        setAnnotationValues = new SetAnnotationValueActionChangeListGeneratorFactory(
                () -> dataFactory,
                () -> projectOntologies,
                () -> index(EntitiesInOntologySignatureIndex.class),
                () -> annotationAssertions);
        editAnnotations = new EditAnnotationsChangeListGeneratorFactory(() -> dataFactory,
                                                                        () -> projectOntologies,
                                                                        () -> annotationAssertions);
        // Search
        entitySearchers = new EntitySearcherFactory(() -> projectId,
                                                    () -> dictionary,
                                                    context::entityNodeRenderer);
        // Revisions (stage S8): the legacy bindings of OWLObjectComparatorImpl, the axiom subject provider and
        // AxiomComparatorImpl, which order the changes of a revision by subject, axiom type and rendering
        var objectComparator = new OWLObjectComparator(rendering);
        var axiomSubjects = new AxiomSubjectProvider(new OWLClassExpressionSelector(objectComparator),
                                                     new OWLObjectPropertyExpressionSelector(objectComparator),
                                                     new OWLDataPropertyExpressionSelector(objectComparator),
                                                     new OWLIndividualSelector(objectComparator),
                                                     new SWRLAtomSelector(objectComparator));
        var axiomComparator = new AxiomComparator(new AxiomBySubjectComparator(axiomSubjects, objectComparator),
                                                  new AxiomByTypeComparator(DefaultAxiomTypeOrdering.get()),
                                                  new AxiomByRenderingComparator(new OWLObjectRendererImpl(rendering)));
        projectChanges = new ProjectChangesManager(projectId,
                                                   context.revisionManager(),
                                                   rendering,
                                                   new OntologyChangeComparator(axiomComparator, objectComparator),
                                                   () -> new Revision2DiffElementsTranslator(
                                                           ontologyIriShortFormProvider,
                                                           defaultOntologyIdManager,
                                                           projectOntologies));
        revisionDetails = new RevisionDetailsExtractor(axiomSubjects);
        revisionReverters = new RevisionReverterChangeListGeneratorFactory(context::revisionManager);
    }

    /**
     * The frames of classes, with the translation options of the caller's choice.
     */
    @Nonnull
    public ClassFrameProvider classFrameProvider() {
        return classFrameProvider;
    }

    @Nonnull
    public ObjectPropertyFrameTranslator newObjectPropertyFrameTranslator() {
        return new ObjectPropertyFrameTranslator(index(ProjectOntologiesIndex.class),
                                                 index(AnnotationAssertionAxiomsBySubjectIndex.class),
                                                 index(ObjectPropertyDomainAxiomsIndex.class),
                                                 index(ObjectPropertyRangeAxiomsIndex.class),
                                                 index(ObjectPropertyCharacteristicsIndex.class),
                                                 () -> axiomPropertyValueTranslator,
                                                 propertyValue2AxiomTranslator);
    }

    @Nonnull
    public DataPropertyFrameTranslator newDataPropertyFrameTranslator() {
        return new DataPropertyFrameTranslator(index(ProjectOntologiesIndex.class),
                                               index(AnnotationAssertionAxiomsBySubjectIndex.class),
                                               index(DataPropertyDomainAxiomsIndex.class),
                                               index(DataPropertyRangeAxiomsIndex.class),
                                               index(DataPropertyCharacteristicsIndex.class),
                                               () -> axiomPropertyValueTranslator,
                                               propertyValue2AxiomTranslator);
    }

    @Nonnull
    public AnnotationPropertyFrameTranslator newAnnotationPropertyFrameTranslator() {
        return new AnnotationPropertyFrameTranslator(index(ProjectOntologiesIndex.class),
                                                     index(AnnotationAssertionAxiomsBySubjectIndex.class),
                                                     index(AnnotationPropertyDomainAxiomsIndex.class),
                                                     index(AnnotationPropertyRangeAxiomsIndex.class));
    }

    @Nonnull
    public NamedIndividualFrameTranslator newNamedIndividualFrameTranslator() {
        return new NamedIndividualFrameTranslator(index(NamedIndividualFrameAxiomIndex.class),
                                                  propertyValueMinimiser,
                                                  classFrameProvider,
                                                  () -> axiomPropertyValueTranslator,
                                                  propertyValue2AxiomTranslator,
                                                  context.dataFactory());
    }

    /**
     * Renders the components of plain frames, caching the renderings for one request (legacy
     * {@code FrameComponentSessionRendererFactory}).
     */
    @Nonnull
    public FrameComponentSessionRenderer newFrameRenderer() {
        return new FrameComponentSessionRenderer(frameComponentRenderer);
    }

    /**
     * Orders the property values of rendered frames.
     */
    @Nonnull
    public Comparator<PropertyValue> propertyValueComparator() {
        return propertyValueComparator;
    }

    /**
     * Turns an edit of a plain frame into ontology changes, with translators of its own.
     */
    @Nonnull
    public FrameChangeGenerator newFrameChangeGenerator(@Nonnull FrameUpdate frameUpdate) {
        return new FrameChangeGeneratorFactory(index(ProjectOntologiesIndex.class),
                                               changeDescriptions,
                                               context.defaultOntologyIdManager(),
                                               index(OntologyAxiomsIndex.class),
                                               newObjectPropertyFrameTranslator(),
                                               newDataPropertyFrameTranslator(),
                                               newAnnotationPropertyFrameTranslator(),
                                               newNamedIndividualFrameTranslator(),
                                               context.rendering(),
                                               classFrameProvider,
                                               new ClassFrame2FrameAxiomsTranslator(context.dataFactory(),
                                                                                    propertyValue2AxiomTranslator))
                .create(frameUpdate);
    }

    /**
     * Renders annotation properties and values, as in the legacy ontology frames.
     */
    @Nonnull
    public ContextRenderer contextRenderer() {
        return contextRenderer;
    }

    /**
     * The short names of the project's ontologies, as used in Manchester syntax.
     */
    @Nonnull
    public WebProtegeOntologyIRIShortFormProvider ontologyIriShortFormProvider() {
        return ontologyIriShortFormProvider;
    }

    /**
     * Views of the project's ontologies as OWL API ontologies, for the Manchester syntax frame renderer.
     */
    @Nonnull
    public OwlOntologyFacadeFactory ontologyFacadeFactory() {
        return ontologyFacadeFactory;
    }

    /**
     * Entity short forms, quoted when they contain spaces, as Manchester syntax needs them.
     */
    @Nonnull
    public ShortFormProvider newEscapingShortFormProvider() {
        return new EscapingShortFormProvider(context.dictionary());
    }

    @Nonnull
    public ManchesterSyntaxFrameParser newManchesterSyntaxFrameParser() {
        var defaultOntologyIdManager = context.defaultOntologyIdManager();
        return new ManchesterSyntaxFrameParser(new ShellOntologyChecker(index(ProjectOntologiesIndex.class),
                                                                        ontologyIriShortFormProvider,
                                                                        defaultOntologyIdManager),
                                               context.dataFactory(),
                                               context.dictionary(),
                                               defaultOntologyIdManager);
    }

    @Nonnull
    public ManchesterSyntaxChangeGeneratorFactory manchesterSyntaxChangeGeneratorFactory() {
        return manchesterSyntaxChangeGeneratorFactory;
    }

    /**
     * Renders entity frames as HTML Manchester syntax, for the read-only description browser (legacy
     * {@code GetEntityHtmlRendering}).
     */
    @Nonnull
    public ManchesterSyntaxEntityFrameRenderer newEntityFrameHtmlRenderer() {
        var entitiesInSignatureByIri = index(EntitiesInProjectSignatureByIriIndex.class);
        var annotationAssertions = index(AnnotationAssertionAxiomsBySubjectIndex.class);
        var axiomsByType = index(AxiomsByTypeIndex.class);
        var objectRenderer = new ManchesterSyntaxObjectRenderer(shortFormProvider,
                                                                new EntityIRICheckerImpl(entitiesInSignatureByIri),
                                                                LiteralStyle.REGULAR,
                                                                new DefaultHttpLinkRenderer(),
                                                                new MarkdownLiteralRenderer());
        return new ManchesterSyntaxEntityFrameRenderer(
                shortFormProvider,
                ontologyIriShortFormProvider,
                objectRenderer,
                new NullHighlightedEntityChecker(),
                context.deprecatedEntityChecker(),
                new DefaultItemStyleProvider(),
                NestedAnnotationStyle.COMPACT,
                index(ProjectOntologiesIndex.class),
                new OntologyAnnotationsSectionRenderer(index(OntologyAnnotationsIndex.class)),
                new ClassFrameRenderer(new AnnotationsSectionRenderer<>(annotationAssertions),
                                       new ClassSubClassOfSectionRenderer(index(SubClassOfAxiomsBySubClassIndex.class)),
                                       new ClassEquivalentToSectionRenderer(index(EquivalentClassesAxiomsIndex.class)),
                                       new ClassDisjointWithSectionRenderer(index(DisjointClassesAxiomsIndex.class))),
                new ObjectPropertyFrameRenderer(
                        new AnnotationsSectionRenderer<>(annotationAssertions),
                        new ObjectPropertyDomainSectionRenderer(index(ObjectPropertyDomainAxiomsIndex.class)),
                        new ObjectPropertyRangeSectionRenderer(index(ObjectPropertyRangeAxiomsIndex.class)),
                        new ObjectPropertyCharacteristicsSectionRenderer(axiomsByType),
                        new ObjectPropertySubPropertyOfRenderer(index(SubObjectPropertyAxiomsBySubPropertyIndex.class)),
                        new ObjectPropertyEquivalentToSectionRenderer(
                                index(EquivalentObjectPropertiesAxiomsIndex.class)),
                        new ObjectPropertyDisjointWithSectionRenderer(
                                index(DisjointObjectPropertiesAxiomsIndex.class)),
                        new ObjectPropertyInverseOfSectionRenderer(index(InverseObjectPropertyAxiomsIndex.class)),
                        new ObjectPropertySubPropertyChainSectionRenderer(axiomsByType)),
                new NamedIndividualFrameRenderer(
                        new NamedIndividualTypesSectionRenderer(index(ClassAssertionAxiomsByIndividualIndex.class)),
                        new AnnotationsSectionRenderer<>(annotationAssertions),
                        new NamedIndividualFactsSectionRenderer(
                                index(ObjectPropertyAssertionAxiomsBySubjectIndex.class),
                                index(DataPropertyAssertionAxiomsBySubjectIndex.class)),
                        new NamedIndividualSameAsSectionRenderer(index(SameIndividualAxiomsIndex.class)),
                        new NamedIndividualDifferentFromSectionRenderer(index(DifferentIndividualsAxiomsIndex.class))),
                new AnnotationPropertyFrameRenderer(
                        new AnnotationsSectionRenderer<>(annotationAssertions),
                        new AnnotationPropertyDomainSectionRenderer(index(AnnotationPropertyDomainAxiomsIndex.class),
                                                                    entitiesInSignatureByIri),
                        new AnnotationPropertyRangeSectionRenderer(index(AnnotationPropertyRangeAxiomsIndex.class),
                                                                   entitiesInSignatureByIri),
                        new AnnotationPropertySubPropertyOfSectionRenderer(
                                index(SubAnnotationPropertyAxiomsBySubPropertyIndex.class))),
                new DataPropertyFrameRenderer(
                        new AnnotationsSectionRenderer<>(annotationAssertions),
                        new DataPropertyDomainSectionRenderer(index(DataPropertyDomainAxiomsIndex.class)),
                        new DataPropertyRangeSectionRenderer(index(DataPropertyRangeAxiomsIndex.class)),
                        new DataPropertyCharacteristicsSectionRenderer(axiomsByType),
                        new DataPropertySubPropertyOfSectionRenderer(
                                index(SubDataPropertyAxiomsBySubPropertyIndex.class)),
                        new DataPropertyEquivalentToSectionRenderer(index(EquivalentDataPropertiesAxiomsIndex.class)),
                        new DataPropertyDisjointWithSectionRenderer(
                                index(DisjointDataPropertiesAxiomsIndex.class))));
    }

    @Nonnull
    public EntityRenamer entityRenamer() {
        return entityRenamer;
    }

    @Nonnull
    public CreateClassesChangeGeneratorFactory createClassesChangeGeneratorFactory() {
        return createClasses;
    }

    @Nonnull
    public CreateObjectPropertiesChangeGeneratorFactory createObjectPropertiesChangeGeneratorFactory() {
        return createObjectProperties;
    }

    @Nonnull
    public CreateDataPropertiesChangeGeneratorFactory createDataPropertiesChangeGeneratorFactory() {
        return createDataProperties;
    }

    @Nonnull
    public CreateAnnotationPropertiesChangeGeneratorFactory createAnnotationPropertiesChangeGeneratorFactory() {
        return createAnnotationProperties;
    }

    @Nonnull
    public CreateIndividualsChangeListGeneratorFactory createIndividualsChangeListGeneratorFactory() {
        return createIndividuals;
    }

    @Nonnull
    public DeleteEntitiesChangeListGeneratorFactory deleteEntitiesChangeListGeneratorFactory() {
        return deleteEntities;
    }

    @Nonnull
    public MergeEntitiesChangeListGeneratorFactory mergeEntitiesChangeListGeneratorFactory() {
        return mergeEntities;
    }

    /**
     * Finds where an entity is used (legacy {@code GetUsage}).
     */
    @Nonnull
    public ReferencingAxiomVisitorFactory referencingAxiomVisitorFactory() {
        return referencingAxiomVisitors;
    }

    @Nonnull
    public HierarchyProviderMapper hierarchyProviderMapper() {
        return hierarchyProviders;
    }

    @Nonnull
    public GraphNodeRenderer graphNodeRenderer() {
        return graphNodeRenderer;
    }

    @Nonnull
    public MoveEntityChangeListGeneratorFactory moveEntityChangeListGeneratorFactory() {
        return moveEntities;
    }

    @Nonnull
    public MoveClassesChangeListGeneratorFactory moveClassesChangeListGeneratorFactory() {
        return moveClasses;
    }

    @Nonnull
    public SetAnnotationValueActionChangeListGeneratorFactory setAnnotationValueChangeListGeneratorFactory() {
        return setAnnotationValues;
    }

    @Nonnull
    public EditAnnotationsChangeListGeneratorFactory editAnnotationsChangeListGeneratorFactory() {
        return editAnnotations;
    }

    @Nonnull
    public EntitySearcherFactory entitySearcherFactory() {
        return entitySearchers;
    }

    /**
     * The project's changes, a revision at a time and rendered as diffs (legacy {@code GetProjectChanges}).
     */
    @Nonnull
    public ProjectChangesManager projectChangesManager() {
        return projectChanges;
    }

    /**
     * The axioms that a revision added or removed, with their subjects.
     */
    @Nonnull
    public RevisionDetailsExtractor revisionDetailsExtractor() {
        return revisionDetails;
    }

    @Nonnull
    public RevisionReverterChangeListGeneratorFactory revisionReverterChangeListGeneratorFactory() {
        return revisionReverters;
    }

    private <T> T index(Class<T> indexClass) {
        return context.indexes().get(indexClass);
    }

    /**
     * The change descriptions of frame and Manchester syntax edits, with the change matchers that the legacy
     * {@code ProjectModule} bound.
     */
    private ReverseEngineeredChangeDescriptionGeneratorFactory newChangeDescriptions() {
        var dictionary = context.dictionary();
        var stringFormatter = new OWLObjectStringFormatter(
                shortFormProvider,
                new IriShortFormAdapter(index(EntitiesInProjectSignatureByIriIndex.class), dictionary),
                new StringFormatterLiteralRenderer(shortFormProvider, lexicalForm -> lexicalForm));
        Set<ChangeMatcher> changeMatchers = Set.of(new AnnotationAssertionChangeMatcher(),
                                                   new PropertyDomainAxiomChangeMatcher(),
                                                   new PropertyRangeAxiomChangeMatcher(),
                                                   new EditedAnnotationAssertionChangeMatcher(stringFormatter,
                                                                                              langTag -> langTag),
                                                   new FunctionalDataPropertyAxiomChangeMatcher(),
                                                   new ClassAssertionAxiomMatcher(),
                                                   new SubClassOfAxiomMatcher(stringFormatter),
                                                   new ClassMoveChangeMatcher(stringFormatter),
                                                   new SubClassOfEditChangeMatcher(),
                                                   new PropertyAssertionAxiomMatcher(),
                                                   new SameIndividualAxiomChangeMatcher(),
                                                   new EntityCreationMatcher(stringFormatter),
                                                   new EntityDeletionMatcher());
        return new ReverseEngineeredChangeDescriptionGeneratorFactory(changeMatchers, stringFormatter);
    }

    private OwlOntologyFacadeFactory newOntologyFacadeFactory() {
        var dataFactory = context.dataFactory();
        return new OwlOntologyFacadeFactory(() -> index(OntologyAnnotationsIndex.class),
                                            () -> index(OntologySignatureIndex.class),
                                            () -> index(OntologyAxiomsIndex.class),
                                            () -> index(EntitiesInOntologySignatureByIriIndex.class),
                                            () -> index(AnnotationAssertionAxiomsBySubjectIndex.class),
                                            () -> dataFactory,
                                            () -> index(SubAnnotationPropertyAxiomsBySubPropertyIndex.class),
                                            () -> index(AnnotationPropertyDomainAxiomsIndex.class),
                                            () -> index(AnnotationPropertyRangeAxiomsIndex.class),
                                            () -> index(SubClassOfAxiomsBySubClassIndex.class),
                                            () -> index(AxiomsByReferenceIndex.class),
                                            () -> index(EquivalentClassesAxiomsIndex.class),
                                            () -> index(DisjointClassesAxiomsIndex.class),
                                            () -> index(AxiomsByTypeIndex.class),
                                            () -> index(SubObjectPropertyAxiomsBySubPropertyIndex.class),
                                            () -> index(ObjectPropertyDomainAxiomsIndex.class),
                                            () -> index(ObjectPropertyRangeAxiomsIndex.class),
                                            () -> index(InverseObjectPropertyAxiomsIndex.class),
                                            () -> index(EquivalentObjectPropertiesAxiomsIndex.class),
                                            () -> index(DisjointObjectPropertiesAxiomsIndex.class),
                                            () -> index(SubDataPropertyAxiomsBySubPropertyIndex.class),
                                            () -> index(DataPropertyDomainAxiomsIndex.class),
                                            () -> index(DataPropertyRangeAxiomsIndex.class),
                                            () -> index(EquivalentDataPropertiesAxiomsIndex.class),
                                            () -> index(DisjointDataPropertiesAxiomsIndex.class),
                                            () -> index(ClassAssertionAxiomsByIndividualIndex.class),
                                            () -> index(ClassAssertionAxiomsByClassIndex.class),
                                            () -> index(DataPropertyAssertionAxiomsBySubjectIndex.class),
                                            () -> index(ObjectPropertyAssertionAxiomsBySubjectIndex.class),
                                            () -> index(SameIndividualAxiomsIndex.class),
                                            () -> index(DifferentIndividualsAxiomsIndex.class));
    }
}
