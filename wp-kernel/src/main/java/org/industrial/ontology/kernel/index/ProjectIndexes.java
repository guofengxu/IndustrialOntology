package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MutableClassToInstanceMap;
import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.index.BuiltInOwlEntitiesIndexImpl;
import org.industrial.ontology.kernel.api.index.BuiltInSkosEntitiesIndexImpl;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesByEntityIndex;
import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.owlapi.ProjectAnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Every index of one project, wired by hand in constructor-dependency order (docs/01 §3.2 rule 3). Replaces the
 * Dagger bindings of the legacy {@code edu.stanford.bmir.protege.web.server.index.IndexModule}.
 * <p>
 * Each implementation is registered under its own class and under every interface of
 * {@code org.industrial.ontology.kernel.api.index} that it implements directly, so callers look indexes up by the
 * interface they need: {@code indexes.get(ClassFrameAxiomsIndex.class)}. One implementation may answer several
 * interfaces ({@link AxiomsByEntityReferenceIndex} is also the {@code OntologyAxiomsSignatureIndex}); two
 * implementations of the same interface are rejected. Some interfaces, such as {@code RootIndex}, do not extend
 * {@link Index}, so {@link #get(Class)} is not bounded by it.
 * <p>
 * Construction has two steps because four indexes answer queries through components that are themselves built from
 * indexes: {@link IndividualsIndex} and {@link IndividualsByTypeIndex} use the class hierarchy and the dictionary,
 * and the deprecated-entity indexes are backed by the project's Lucene index, exactly as in the legacy bindings.
 * {@link #builder(OWLDataFactory)} creates all axiom-backed indexes; once those components exist,
 * {@link Builder#build} adds the remaining four.
 * <p>
 * The {@link #updatable()} set is the 17 primary indexes that {@link IndexUpdater} feeds with changes; it is fixed by
 * the builder and never contains an index added later.
 */
public final class ProjectIndexes {

    private static final String API_INDEX_PACKAGE = Index.class.getPackageName();

    private final ImmutableClassToInstanceMap<Object> indexesByType;

    private final ImmutableSet<UpdatableIndex> updatable;

    private ProjectIndexes(@Nonnull ImmutableClassToInstanceMap<Object> indexesByType,
                           @Nonnull ImmutableSet<UpdatableIndex> updatable) {
        this.indexesByType = indexesByType;
        this.updatable = updatable;
    }

    /**
     * Creates the project's axiom-backed indexes. Nothing is loaded into them until {@link IndexUpdater#buildIndexes()}
     * replays the project's revisions.
     */
    @Nonnull
    public static Builder builder(@Nonnull OWLDataFactory dataFactory) {
        return new Builder(checkNotNull(dataFactory));
    }

    /**
     * Gets the index registered under {@code type}.
     *
     * @throws IllegalArgumentException if no index is registered under {@code type}
     */
    @Nonnull
    public <T> T get(@Nonnull Class<T> type) {
        return lookUp(indexesByType, type);
    }

    /**
     * The primary indexes, in no particular order, for {@link IndexUpdater}.
     */
    @Nonnull
    public ImmutableSet<UpdatableIndex> updatable() {
        return updatable;
    }

    private static <T> T lookUp(@Nonnull Map<Class<?>, Object> indexesByType, @Nonnull Class<T> type) {
        var index = indexesByType.get(checkNotNull(type));
        checkArgument(index != null, "No index is registered for %s", type.getName());
        return type.cast(index);
    }

    /**
     * Holds the indexes created so far, so that the hierarchy providers, dictionary and Lucene components can be wired
     * from them before {@link #build} completes the set.
     */
    public static final class Builder {

        private final MutableClassToInstanceMap<Object> indexesByType = MutableClassToInstanceMap.create();

        private final ImmutableSet<UpdatableIndex> updatable;

        private final OWLDataFactory dataFactory;

        private boolean built = false;

        private Builder(@Nonnull OWLDataFactory dataFactory) {
            this.dataFactory = dataFactory;

            // Primary indexes: the only ones IndexUpdater writes to.
            var annotationAssertionsBySubject = new AnnotationAssertionAxiomsBySubjectIndex();
            var annotationAssertionsByValue = new AnnotationAssertionAxiomsByValueIndex();
            var annotationAxiomsByIriReference = new AnnotationAxiomsByIriReferenceIndex();
            var axiomsByEntityReference = new AxiomsByEntityReferenceIndex(dataFactory);
            var axiomsByType = new AxiomsByTypeIndex();
            var classAssertionsByClass = new ClassAssertionAxiomsByClassIndex();
            var classAssertionsByIndividual = new ClassAssertionAxiomsByIndividualIndex();
            var dataPropertyAssertionsBySubject = new DataPropertyAssertionAxiomsBySubjectIndex();
            var differentIndividuals = new DifferentIndividualsAxiomsIndex();
            var disjointClasses = new DisjointClassesAxiomsIndex();
            var equivalentClasses = new EquivalentClassesAxiomsIndex();
            var objectPropertyAssertionsBySubject = new ObjectPropertyAssertionAxiomsBySubjectIndex();
            var ontologyAnnotations = new OntologyAnnotationsIndex();
            var projectOntologies = new ProjectOntologiesIndex();
            var sameIndividuals = new SameIndividualAxiomsIndex();
            var subAnnotationPropertiesBySuperProperty = new SubAnnotationPropertyAxiomsBySuperPropertyIndex();
            var subClassOfBySubClass = new SubClassOfAxiomsBySubClassIndex();
            updatable = ImmutableSet.of(annotationAssertionsBySubject,
                                        annotationAssertionsByValue,
                                        annotationAxiomsByIriReference,
                                        axiomsByEntityReference,
                                        axiomsByType,
                                        classAssertionsByClass,
                                        classAssertionsByIndividual,
                                        dataPropertyAssertionsBySubject,
                                        differentIndividuals,
                                        disjointClasses,
                                        equivalentClasses,
                                        objectPropertyAssertionsBySubject,
                                        ontologyAnnotations,
                                        projectOntologies,
                                        sameIndividuals,
                                        subAnnotationPropertiesBySuperProperty,
                                        subClassOfBySubClass);
            updatable.forEach(this::register);

            // Derived indexes over the axiom-type index.
            register(new AnnotationPropertyDomainAxiomsIndex(axiomsByType));
            register(new AnnotationPropertyRangeAxiomsIndex(axiomsByType));
            register(new DataPropertyCharacteristicsIndex(axiomsByType));
            register(new DataPropertyDomainAxiomsIndex(axiomsByType));
            register(new DataPropertyRangeAxiomsIndex(axiomsByType));
            register(new DisjointDataPropertiesAxiomsIndex(axiomsByType));
            register(new DisjointObjectPropertiesAxiomsIndex(axiomsByType));
            register(new EquivalentDataPropertiesAxiomsIndex(axiomsByType));
            register(new EquivalentObjectPropertiesAxiomsIndex(axiomsByType));
            register(new InverseObjectPropertyAxiomsIndex(axiomsByType));
            register(new ObjectPropertyCharacteristicsIndex(axiomsByType));
            register(new ObjectPropertyDomainAxiomsIndex(axiomsByType));
            register(new ObjectPropertyRangeAxiomsIndex(axiomsByType));
            register(new SubAnnotationPropertyAxiomsBySubPropertyIndex(axiomsByType));
            register(new SubDataPropertyAxiomsBySubPropertyIndex(axiomsByType));
            register(new SubObjectPropertyAxiomsBySubPropertyIndex(axiomsByType));
            var ontologyAxioms = new OntologyAxiomsIndex(axiomsByType);
            register(ontologyAxioms);
            register(new RootIndex(ontologyAxioms, ontologyAnnotations));

            // Signatures. AxiomsByEntityReferenceIndex and OntologyAnnotationsIndex double as the ontology signature
            // indexes, as in the legacy bindings.
            var ontologySignature = new OntologySignatureIndex(axiomsByEntityReference);
            register(ontologySignature);
            register(new OntologySignatureByTypeIndex(axiomsByEntityReference, ontologyAnnotations));
            var entitiesInOntologySignature = new EntitiesInOntologySignatureIndex(axiomsByEntityReference,
                                                                                   ontologyAnnotations);
            register(entitiesInOntologySignature);
            var entitiesInOntologySignatureByIri = new EntitiesInOntologySignatureByIriIndex(axiomsByEntityReference,
                                                                                             ontologyAnnotations);
            register(entitiesInOntologySignatureByIri);
            register(new ProjectSignatureIndex(projectOntologies, ontologySignature));
            register(new ProjectSignatureByTypeIndex(axiomsByEntityReference));
            register(new EntitiesInProjectSignatureIndex(projectOntologies, entitiesInOntologySignature));
            register(new EntitiesInProjectSignatureByIriIndex(projectOntologies, entitiesInOntologySignatureByIri));

            // Project-wide views, references and frames.
            var projectAnnotationAssertionsBySubject = new ProjectAnnotationAssertionAxiomsBySubjectIndex(
                    projectOntologies, annotationAssertionsBySubject);
            register(projectAnnotationAssertionsBySubject);
            register(new AnnotationAssertionAxiomsIndexWrapper(projectOntologies,
                                                               axiomsByType,
                                                               projectAnnotationAssertionsBySubject));
            register(new AxiomsByReferenceIndex(axiomsByEntityReference, annotationAxiomsByIriReference));
            register(new ProjectClassAssertionAxiomsByIndividualIndex(projectOntologies, classAssertionsByIndividual));
            var propertyAssertionsBySubject = new PropertyAssertionAxiomsBySubjectIndex(
                    annotationAssertionsBySubject, objectPropertyAssertionsBySubject, dataPropertyAssertionsBySubject);
            register(propertyAssertionsBySubject);
            register(new ClassFrameAxiomsIndex(projectOntologies,
                                               subClassOfBySubClass,
                                               equivalentClasses,
                                               annotationAssertionsBySubject));
            register(new NamedIndividualFrameAxiomsIndex(projectOntologies,
                                                         classAssertionsByIndividual,
                                                         propertyAssertionsBySubject,
                                                         sameIndividuals));

            // Built-in vocabularies.
            register(new BuiltInOwlEntitiesIndexImpl(dataFactory));
            register(new BuiltInSkosEntitiesIndexImpl(dataFactory));
        }

        /**
         * Gets an index created so far.
         *
         * @throws IllegalArgumentException if no index is registered under {@code type}
         */
        @Nonnull
        public <T> T get(@Nonnull Class<T> type) {
            return lookUp(indexesByType, type);
        }

        /**
         * The primary indexes, for {@link IndexUpdater}.
         */
        @Nonnull
        public ImmutableSet<UpdatableIndex> updatable() {
            return updatable;
        }

        /**
         * Adds the indexes that query the class hierarchy, the dictionary and the project's Lucene index, and returns
         * the complete set. The builder cannot be used afterwards.
         */
        @Nonnull
        public ProjectIndexes build(@Nonnull ClassHierarchyProvider classHierarchyProvider,
                                    @Nonnull DictionaryManager dictionaryManager,
                                    @Nonnull DeprecatedEntitiesIndex deprecatedEntitiesIndex,
                                    @Nonnull DeprecatedEntitiesByEntityIndex deprecatedEntitiesByEntityIndex) {
            checkState(!built, "The project indexes have already been built");
            var projectOntologies = get(org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex.class);
            var classAssertionsByIndividual = get(ClassAssertionAxiomsByIndividualIndex.class);
            var individualsByType = new IndividualsByTypeIndex(projectOntologies,
                                                               get(ProjectSignatureByTypeIndex.class),
                                                               classAssertionsByIndividual,
                                                               get(ClassAssertionAxiomsByClassIndex.class),
                                                               checkNotNull(classHierarchyProvider),
                                                               checkNotNull(dictionaryManager),
                                                               dataFactory);
            register(individualsByType);
            register(new IndividualsIndex(projectOntologies,
                                          classAssertionsByIndividual,
                                          dictionaryManager,
                                          classHierarchyProvider,
                                          dataFactory,
                                          individualsByType));
            register(checkNotNull(deprecatedEntitiesIndex));
            register(checkNotNull(deprecatedEntitiesByEntityIndex));
            built = true;
            return new ProjectIndexes(ImmutableClassToInstanceMap.copyOf(indexesByType), updatable);
        }

        private void register(@Nonnull Object index) {
            put(index.getClass(), index);
            for(var type : index.getClass().getInterfaces()) {
                if(isApiIndexType(type)) {
                    put(type, index);
                }
            }
        }

        private void put(@Nonnull Class<?> type, @Nonnull Object index) {
            var previous = indexesByType.putIfAbsent(type, index);
            checkState(previous == null,
                       "Both %s and %s implement %s",
                       previous == null ? "" : previous.getClass().getName(),
                       index.getClass().getName(),
                       type.getName());
        }

        private static boolean isApiIndexType(@Nonnull Class<?> type) {
            return type.getPackageName().equals(API_INDEX_PACKAGE)
                    && type != Index.class
                    && type != DependentIndex.class;
        }
    }
}
