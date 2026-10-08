package org.industrial.ontology.kernel.form;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyCharacteristicsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.NamedIndividualFrameAxiomIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyCharacteristicsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.change.ReverseEngineeredChangeDescriptionGeneratorFactory;
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
import org.industrial.ontology.kernel.form.processor.FormControlDataProcessor;
import org.industrial.ontology.kernel.form.processor.FormDataConverter;
import org.industrial.ontology.kernel.form.processor.FormDataProcessor;
import org.industrial.ontology.kernel.form.processor.FormFieldProcessor;
import org.industrial.ontology.kernel.form.processor.GridCellDataProcessor;
import org.industrial.ontology.kernel.form.processor.GridControlDataProcessor;
import org.industrial.ontology.kernel.form.processor.GridRowDataProcessor;
import org.industrial.ontology.kernel.frame.ClassFrameProviderImpl;
import org.industrial.ontology.kernel.frame.EmptyEntityFrameFactory;
import org.industrial.ontology.kernel.frame.FrameChangeGeneratorFactory;
import org.industrial.ontology.kernel.frame.FrameComponentRenderer;
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
import org.industrial.ontology.kernel.hierarchy.NamedIndividualClassAncestorChecker;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyObjectPropertyAncestorChecker;
import org.industrial.ontology.kernel.match.LiteralMatcherFactory;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.industrial.ontology.kernel.owlapi.StringFormatterLiteralRenderer;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.render.ContextRenderer;
import org.industrial.ontology.kernel.render.ShortFormAdapter;
import org.industrial.ontology.kernel.shortform.IriShortFormAdapter;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The form use cases' collaborators for one project, wired from its {@link ProjectContext} as the legacy
 * {@code ProjectModule} bound them: {@link EntityFrameFormDataDtoBuilderFactory} reads an entity's form data, and
 * {@link EntityFormChangeListGeneratorFactory} turns edited form data into changes. The P1 form services (S9) will
 * build the same graph.
 */
final class ProjectFormComponents {

    private final EntityFrameFormDataDtoBuilderFactory formDataDtoBuilderFactory;

    private final EntityFormChangeListGeneratorFactory changeListGeneratorFactory;

    ProjectFormComponents(@Nonnull ProjectContext context) {
        var dataFactory = context.dataFactory();
        var indexes = context.indexes();
        var rendering = context.rendering();
        var hierarchies = context.hierarchies();
        var matcherFactory = context.matcherFactory();
        var projectOntologies = indexes.get(ProjectOntologiesIndex.class);
        var entitiesInSignatureByIri = indexes.get(EntitiesInProjectSignatureByIriIndex.class);
        var classAssertionsByIndividual = indexes.get(ClassAssertionAxiomsByIndividualIndex.class);
        var annotationAssertionsBySubject = indexes.get(AnnotationAssertionAxiomsBySubjectIndex.class);

        // Frames
        var classAncestors = new ClassClassAncestorChecker(hierarchies.classHierarchy());
        var propertyValueMinimiser = new PropertyValueMinimiser(new StructuralPropertyValueSubsumptionChecker(
                classAncestors,
                new ObjectPropertyObjectPropertyAncestorChecker(hierarchies.objectPropertyHierarchy()),
                new DataPropertyDataPropertyAncestorChecker(hierarchies.dataPropertyHierarchy()),
                new NamedIndividualClassAncestorChecker(classAncestors, classAssertionsByIndividual,
                                                        projectOntologies)));
        var classExpressionTranslator = new ClassExpression2PropertyValuesTranslator();
        var axiomPropertyValueTranslator = new AxiomPropertyValueTranslator(new AxiomTranslatorFactory(
                new SubClassOfAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new EquivalentClassesAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ClassAssertionAxiom2PropertyValuesTranslator(classExpressionTranslator),
                new ObjectPropertyAssertionAxiom2PropertyValuesTranslator(),
                new DataPropertyAssertionAxiom2PropertyValuesTranslator(),
                new AnnotationAssertionAxiom2PropertyValuesTranslator(new Annotation2PropertyValueTranslator())));
        var classFrameProvider = new ClassFrameProviderImpl(new Class2ClassFrameTranslatorFactory(
                () -> indexes.get(ClassFrameAxiomsIndex.class),
                hierarchies::classHierarchy,
                () -> propertyValueMinimiser,
                () -> axiomPropertyValueTranslator,
                () -> matcherFactory));
        var propertyValue2AxiomTranslator = new PropertyValue2AxiomTranslator();

        // Reading form data
        var bindingValuesExtractor = new BindingValuesExtractor(
                projectOntologies,
                classAssertionsByIndividual,
                hierarchies.classHierarchy(),
                indexes.get(ObjectPropertyAssertionAxiomsBySubjectIndex.class),
                indexes.get(DataPropertyAssertionAxiomsBySubjectIndex.class),
                annotationAssertionsBySubject,
                indexes.get(ClassAssertionAxiomsByClassIndex.class),
                classFrameProvider);
        formDataDtoBuilderFactory = new EntityFrameFormDataDtoBuilderFactory(
                new FrameComponentRenderer(rendering, entitiesInSignatureByIri, entitiesInSignatureByIri),
                bindingValuesExtractor,
                entitiesInSignatureByIri,
                context.matchingEngine(),
                new FormFilterMatcherFactory(new LiteralMatcherFactory(), matcherFactory));

        // Writing form data
        var shortFormProvider = new ShortFormAdapter(context.dictionary());
        var stringFormatter = new OWLObjectStringFormatter(shortFormProvider,
                                                           new IriShortFormAdapter(entitiesInSignatureByIri,
                                                                                   context.dictionary()),
                                                           new StringFormatterLiteralRenderer(shortFormProvider,
                                                                                              lexicalForm -> lexicalForm));
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
        var changeDescriptions = new ReverseEngineeredChangeDescriptionGeneratorFactory(changeMatchers,
                                                                                        stringFormatter);
        var frameChangeGeneratorFactory = new FrameChangeGeneratorFactory(
                projectOntologies,
                changeDescriptions,
                context.defaultOntologyIdManager(),
                indexes.get(OntologyAxiomsIndex.class),
                new ObjectPropertyFrameTranslator(projectOntologies,
                                                  annotationAssertionsBySubject,
                                                  indexes.get(ObjectPropertyDomainAxiomsIndex.class),
                                                  indexes.get(ObjectPropertyRangeAxiomsIndex.class),
                                                  indexes.get(ObjectPropertyCharacteristicsIndex.class),
                                                  () -> axiomPropertyValueTranslator,
                                                  propertyValue2AxiomTranslator),
                new DataPropertyFrameTranslator(projectOntologies,
                                                annotationAssertionsBySubject,
                                                indexes.get(DataPropertyDomainAxiomsIndex.class),
                                                indexes.get(DataPropertyRangeAxiomsIndex.class),
                                                indexes.get(DataPropertyCharacteristicsIndex.class),
                                                () -> axiomPropertyValueTranslator,
                                                propertyValue2AxiomTranslator),
                new AnnotationPropertyFrameTranslator(projectOntologies,
                                                      annotationAssertionsBySubject,
                                                      indexes.get(AnnotationPropertyDomainAxiomsIndex.class),
                                                      indexes.get(AnnotationPropertyRangeAxiomsIndex.class)),
                new NamedIndividualFrameTranslator(indexes.get(NamedIndividualFrameAxiomIndex.class),
                                                   propertyValueMinimiser,
                                                   classFrameProvider,
                                                   () -> axiomPropertyValueTranslator,
                                                   propertyValue2AxiomTranslator,
                                                   dataFactory),
                rendering,
                classFrameProvider,
                new ClassFrame2FrameAxiomsTranslator(dataFactory, propertyValue2AxiomTranslator));
        // Form data processors refer to each other through suppliers, as the legacy providers did.
        var controlDataProcessor = new AtomicReference<FormControlDataProcessor>();
        var dataProcessor = new AtomicReference<FormDataProcessor>();
        controlDataProcessor.set(new FormControlDataProcessor(
                new GridControlDataProcessor(new GridRowDataProcessor(FormFrameBuilder::new,
                                                                      new GridCellDataProcessor(
                                                                              controlDataProcessor::get))),
                dataProcessor::get));
        dataProcessor.set(new FormDataProcessor(FormFrameBuilder::new,
                                                new FormFieldProcessor(controlDataProcessor.get())));
        changeListGeneratorFactory = new EntityFormChangeListGeneratorFactory(
                new FormDataConverter(new FormSubjectResolver(new EntityFormSubjectFactory(dataFactory)),
                                      dataProcessor.get()),
                changeDescriptions,
                new MessageFormatter(rendering),
                frameChangeGeneratorFactory,
                new FormFrameConverter(),
                new EmptyEntityFrameFactory(new ContextRenderer(rendering)),
                rendering,
                dataFactory,
                context.defaultOntologyIdManager());
    }

    @Nonnull
    EntityFrameFormDataDtoBuilderFactory formDataDtoBuilderFactory() {
        return formDataDtoBuilderFactory;
    }

    @Nonnull
    EntityFormChangeListGeneratorFactory changeListGeneratorFactory() {
        return changeListGeneratorFactory;
    }
}
