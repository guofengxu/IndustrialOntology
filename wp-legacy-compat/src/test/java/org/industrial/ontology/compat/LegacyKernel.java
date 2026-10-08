package org.industrial.ontology.compat;

import edu.stanford.bmir.protege.web.server.change.OntologyChangeRecordTranslatorImpl;
import edu.stanford.bmir.protege.web.server.hierarchy.ClassHierarchyProviderImpl;
import edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsBySubjectIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsByValueIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.AnnotationAxiomsByIriReferenceIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.AxiomsByEntityReferenceIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.AxiomsByTypeIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ClassAssertionAxiomsByClassIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ClassAssertionAxiomsByIndividualIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ClassFrameAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.DataPropertyAssertionAxiomsBySubjectIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.DifferentIndividualsAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.DisjointClassesAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.EntitiesInOntologySignatureByIriIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.EntitiesInProjectSignatureByIriIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.EquivalentClassesAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.IndexUpdater;
import edu.stanford.bmir.protege.web.server.index.impl.ObjectPropertyAssertionAxiomsBySubjectIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.OntologyAnnotationsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.OntologyAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.OntologySignatureIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ProjectOntologiesIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ProjectSignatureByTypeIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.ProjectSignatureIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.SameIndividualAxiomsIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.SubAnnotationPropertyAxiomsBySuperPropertyIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.SubClassOfAxiomsBySubClassIndexImpl;
import edu.stanford.bmir.protege.web.server.index.impl.UpdatableIndex;
import edu.stanford.bmir.protege.web.server.inject.ChangeHistoryFileFactory;
import edu.stanford.bmir.protege.web.server.inject.project.ProjectDirectoryFactory;
import edu.stanford.bmir.protege.web.server.revision.HeadRevisionNumberFinder;
import edu.stanford.bmir.protege.web.server.revision.Revision;
import edu.stanford.bmir.protege.web.server.revision.RevisionManagerImpl;
import edu.stanford.bmir.protege.web.server.revision.RevisionStoreImpl;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * The legacy webprotege-server-core kernel over a data directory: writes revisions with its BinaryOWL store, and
 * loads them into the indexes and class hierarchy that the compatibility tests compare, wired as the legacy
 * {@code IndexModule} and {@code ProjectModule} bound them.
 * <p>
 * All 17 primary indexes are replayed by the legacy {@link IndexUpdater}. {@link ProjectOntologiesIndexImpl#init} is
 * called after the replay, as {@code ProjectContextFactory} does, so both kernels count each ontology once; when the
 * legacy Dagger graph called it before the replay it counted ontologies twice, which only shows when an ontology loses
 * all its axioms.
 */
final class LegacyKernel {

    final RevisionManagerImpl revisionManager;

    final ProjectOntologiesIndexImpl projectOntologies = new ProjectOntologiesIndexImpl();

    final OntologyAxiomsIndexImpl ontologyAxioms;

    final ClassFrameAxiomsIndexImpl classFrameAxioms;

    final SubClassOfAxiomsBySubClassIndexImpl subClassOfAxioms = new SubClassOfAxiomsBySubClassIndexImpl();

    final AnnotationAssertionAxiomsBySubjectIndexImpl annotationAssertionsBySubject =
            new AnnotationAssertionAxiomsBySubjectIndexImpl();

    final ProjectSignatureIndexImpl projectSignature;

    final ClassHierarchyProviderImpl classHierarchy;

    /**
     * Nanoseconds spent reading the revisions and replaying them into the indexes, for the load-time baseline.
     */
    final long revisionsNanos;

    final long indexesNanos;

    private final RevisionStoreImpl revisionStore;

    private LegacyKernel(@Nonnull Path dataDirectory, @Nonnull String projectId, @Nonnull ExecutorService indexUpdates) {
        var start = System.nanoTime();
        var dataFactory = new OWLDataFactoryImpl();
        var legacyProjectId = ProjectId.get(projectId);
        revisionStore = newRevisionStore(dataDirectory, legacyProjectId, dataFactory);
        revisionStore.load();
        revisionManager = new RevisionManagerImpl(revisionStore);
        var revisionsLoaded = System.nanoTime();
        revisionsNanos = revisionsLoaded - start;

        var axiomsByType = new AxiomsByTypeIndexImpl();
        var axiomsByEntityReference = new AxiomsByEntityReferenceIndexImpl(dataFactory);
        var ontologyAnnotations = new OntologyAnnotationsIndexImpl();
        var equivalentClasses = new EquivalentClassesAxiomsIndexImpl();
        Set<UpdatableIndex> updatable = Set.of(annotationAssertionsBySubject,
                                               new AnnotationAssertionAxiomsByValueIndexImpl(),
                                               new AnnotationAxiomsByIriReferenceIndexImpl(),
                                               axiomsByEntityReference,
                                               axiomsByType,
                                               new ClassAssertionAxiomsByClassIndexImpl(),
                                               new ClassAssertionAxiomsByIndividualIndexImpl(),
                                               new DataPropertyAssertionAxiomsBySubjectIndexImpl(),
                                               new DifferentIndividualsAxiomsIndexImpl(),
                                               new DisjointClassesAxiomsIndexImpl(),
                                               equivalentClasses,
                                               new ObjectPropertyAssertionAxiomsBySubjectIndexImpl(),
                                               ontologyAnnotations,
                                               projectOntologies,
                                               new SameIndividualAxiomsIndexImpl(),
                                               new SubAnnotationPropertyAxiomsBySuperPropertyIndexImpl(),
                                               subClassOfAxioms);
        new IndexUpdater(revisionManager, updatable, indexUpdates, legacyProjectId).buildIndexes();
        projectOntologies.init(revisionManager);
        indexesNanos = System.nanoTime() - revisionsLoaded;

        ontologyAxioms = new OntologyAxiomsIndexImpl(axiomsByType);
        classFrameAxioms = new ClassFrameAxiomsIndexImpl(projectOntologies,
                                                         subClassOfAxioms,
                                                         equivalentClasses,
                                                         annotationAssertionsBySubject);
        projectSignature = new ProjectSignatureIndexImpl(projectOntologies,
                                                         new OntologySignatureIndexImpl(axiomsByEntityReference));
        classHierarchy = new ClassHierarchyProviderImpl(
                legacyProjectId,
                dataFactory.getOWLThing(),
                projectOntologies,
                subClassOfAxioms,
                equivalentClasses,
                new ProjectSignatureByTypeIndexImpl(axiomsByEntityReference),
                axiomsByEntityReference,
                new EntitiesInProjectSignatureByIriIndexImpl(
                        projectOntologies,
                        new EntitiesInOntologySignatureByIriIndexImpl(axiomsByEntityReference, ontologyAnnotations)));
    }

    /**
     * Loads the project's revisions into the legacy indexes and class hierarchy.
     */
    static LegacyKernel load(@Nonnull Path dataDirectory,
                             @Nonnull String projectId,
                             @Nonnull ExecutorService indexUpdates) {
        return new LegacyKernel(dataDirectory, projectId, indexUpdates);
    }

    /**
     * Writes {@code revisions} after the project's existing ones with the legacy revision manager and waits until
     * every one is on disk.
     */
    static void writeRevisions(@Nonnull Path dataDirectory,
                               @Nonnull String projectId,
                               @Nonnull List<CompatDatasets.RevisionSpec> revisions) throws InterruptedException {
        var store = newRevisionStore(dataDirectory, ProjectId.get(projectId), new OWLDataFactoryImpl());
        store.load();
        var saved = new CountDownLatch(revisions.size());
        store.setSavedHook(saved::countDown);
        var revisionManager = new RevisionManagerImpl(store);
        for(var revision : revisions) {
            revisionManager.addRevision(UserId.getUserId(revision.user()),
                                        revision.changes().stream().map(NeutralChange::toLegacy).collect(toList()),
                                        revision.description());
        }
        if(!saved.await(2, TimeUnit.MINUTES)) {
            throw new IllegalStateException("The legacy store did not save all revisions");
        }
        store.dispose();
    }

    /**
     * The revisions of the project as the legacy store reads them.
     */
    static List<Revision> readRevisions(@Nonnull Path dataDirectory, @Nonnull String projectId) {
        var store = newRevisionStore(dataDirectory, ProjectId.get(projectId), new OWLDataFactoryImpl());
        store.load();
        try {
            return store.getRevisions();
        } finally {
            store.dispose();
        }
    }

    /**
     * The head revision number reported by the legacy {@code GetHeadRevisionNumber} path.
     */
    static long headRevisionNumber(@Nonnull Path dataDirectory, @Nonnull String projectId) throws IOException {
        return new HeadRevisionNumberFinder(changeHistoryFileFactory(dataDirectory))
                .getHeadRevisionNumber(ProjectId.get(projectId))
                .getValue();
    }

    void dispose() {
        revisionStore.dispose();
    }

    private static RevisionStoreImpl newRevisionStore(Path dataDirectory,
                                                      ProjectId projectId,
                                                      OWLDataFactoryImpl dataFactory) {
        return new RevisionStoreImpl(projectId,
                                     changeHistoryFileFactory(dataDirectory),
                                     dataFactory,
                                     new OntologyChangeRecordTranslatorImpl());
    }

    private static ChangeHistoryFileFactory changeHistoryFileFactory(Path dataDirectory) {
        return new ChangeHistoryFileFactory(new ProjectDirectoryFactory(dataDirectory.toFile()));
    }
}
