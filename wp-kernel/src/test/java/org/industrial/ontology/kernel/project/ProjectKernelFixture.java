package org.industrial.ontology.kernel.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.api.shortform.EntityShortFormMatches;
import org.industrial.ontology.kernel.api.shortform.SearchString;
import org.industrial.ontology.kernel.change.CreateClassesChangeGeneratorFactory;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslatorImpl;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.io.upload.ProjectImporter;
import org.industrial.ontology.kernel.io.upload.RootOntologyDocumentMatcher;
import org.industrial.ontology.kernel.io.upload.SingleDocumentProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.UploadedOntologiesProcessor;
import org.industrial.ontology.kernel.io.upload.UploadedProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.ZipArchiveProjectSourcesExtractor;
import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.industrial.ontology.kernel.util.TempFileFactoryImpl;
import org.industrial.ontology.kernel.util.ZipInputStreamChecker;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * A kernel over a data directory, standing in for wp-app's {@code KernelExecutors} and {@code ProjectRegistry} in
 * tests: it owns the shared thread pools and shuts them down in {@link #close()}, after the test has closed its
 * projects.
 */
public final class ProjectKernelFixture implements AutoCloseable {

    public static final UserId USER = UserId.getUserId("modeller");

    private final ExecutorService indexUpdates = Executors.newFixedThreadPool(4);

    private final ExecutorService revisionWrites = Executors.newFixedThreadPool(2);

    private final ScheduledExecutorService eventPurges = Executors.newSingleThreadScheduledExecutor();

    private final KernelThreadPools threadPools = new KernelThreadPools(indexUpdates, revisionWrites, eventPurges);

    private final DataDirectoryLayout dataDirectoryLayout;

    private final InMemoryProjectPorts ports = new InMemoryProjectPorts();

    private final ProjectContextFactory factory;

    public ProjectKernelFixture(@Nonnull Path dataDirectory) {
        dataDirectoryLayout = new DataDirectoryLayout(dataDirectory);
        var builtInPrefixDeclarations = new BuiltInPrefixDeclarationsLoader(
                dataDirectoryLayout.getOverridableFileFactory()).getBuiltInPrefixDeclarations();
        factory = new ProjectContextFactory(dataDirectoryLayout,
                                            threadPools,
                                            ports,
                                            builtInPrefixDeclarations,
                                            ProjectEventManager.DEFAULT_RETENTION);
    }

    @Nonnull
    public static ProjectId freshProjectId() {
        return ProjectId.get(UUID.randomUUID().toString());
    }

    /**
     * Creates one class labelled {@code name}@en under {@code parents}, as the create-classes use case does.
     */
    @Nonnull
    public static OWLClass createClass(@Nonnull ProjectContext context,
                                       @Nonnull String name,
                                       @Nonnull ImmutableSet<OWLClass> parents) {
        var generators = new CreateClassesChangeGeneratorFactory(context::dataFactory,
                                                                 () -> new MessageFormatter(context.rendering()),
                                                                 context::defaultOntologyIdManager);
        var created = context.changeManager().applyChanges(USER, generators.create(name, "en", parents)).getSubject();
        if(created.size() != 1) {
            throw new IllegalStateException("Expected one class for " + name + " but got " + created);
        }
        return created.iterator().next();
    }

    /**
     * Searches the project's classes in its display languages, as entity search does.
     */
    @Nonnull
    public static List<OWLEntity> searchClasses(@Nonnull ProjectContext context, @Nonnull String text) {
        return context.dictionary()
                      .getShortFormsContaining(List.of(SearchString.parseSearchString(text)),
                                               Set.of(EntityType.CLASS),
                                               context.languageManager().getLanguages(),
                                               ImmutableList.of(),
                                               PageRequest.requestFirstPage())
                      .getPageElements()
                      .stream()
                      .map(EntityShortFormMatches::entity)
                      .collect(toList());
    }

    @Nonnull
    public ProjectContext open(@Nonnull ProjectId projectId) {
        return factory.create(projectId);
    }

    /**
     * Creates a project from an uploaded ontology document or zip archive, through the same upload directory,
     * extractors and {@link ProjectImporter} as a real upload, and returns the new project's id.
     */
    @Nonnull
    public ProjectId importProject(@Nonnull Path source) throws IOException, OWLOntologyCreationException {
        var projectId = freshProjectId();
        var uploadsDirectory = dataDirectoryLayout.getUploadsDirectory();
        Files.createDirectories(uploadsDirectory);
        var documentId = new DocumentId(UUID.randomUUID().toString());
        Files.copy(source, uploadsDirectory.resolve(documentId.getDocumentId()));
        var documentResolver = dataDirectoryLayout.getDocumentResolver();
        var uploadedOntologiesProcessor = new UploadedOntologiesProcessor(
                documentResolver,
                () -> new UploadedProjectSourcesExtractor(new ZipInputStreamChecker(),
                                                          new ZipArchiveProjectSourcesExtractor(
                                                                  new TempFileFactoryImpl(),
                                                                  new RootOntologyDocumentMatcher()),
                                                          new SingleDocumentProjectSourcesExtractor()));
        var revisionStoreFactory = new RevisionStoreFactory(dataDirectoryLayout.getChangeHistoryFileFactory(),
                                                            new OWLDataFactoryImpl(),
                                                            new OntologyChangeRecordTranslatorImpl(),
                                                            revisionWrites);
        new ProjectImporter(projectId, uploadedOntologiesProcessor, documentResolver, revisionStoreFactory)
                .createProjectFromSources(documentId, USER);
        return projectId;
    }

    /**
     * Copies a test resource, such as {@code /ontologies/pizza/pizza.owl}, to a file.
     */
    @Nonnull
    public static Path copyResource(@Nonnull String resourceName, @Nonnull Path directory) throws IOException {
        try(var in = ProjectKernelFixture.class.getResourceAsStream(resourceName)) {
            if(in == null) {
                throw new IOException("Missing test resource " + resourceName);
            }
            var file = directory.resolve(Path.of(resourceName).getFileName().toString());
            Files.copy(in, file);
            return file;
        }
    }

    @Nonnull
    public ProjectContextFactory factory() {
        return factory;
    }

    @Nonnull
    public KernelThreadPools threadPools() {
        return threadPools;
    }

    @Nonnull
    public DataDirectoryLayout dataDirectoryLayout() {
        return dataDirectoryLayout;
    }

    @Nonnull
    public InMemoryProjectPorts ports() {
        return ports;
    }

    @Override
    public void close() throws InterruptedException {
        indexUpdates.shutdown();
        revisionWrites.shutdown();
        eventPurges.shutdown();
        indexUpdates.awaitTermination(10, TimeUnit.SECONDS);
        revisionWrites.awaitTermination(10, TimeUnit.SECONDS);
        eventPurges.awaitTermination(10, TimeUnit.SECONDS);
    }
}
