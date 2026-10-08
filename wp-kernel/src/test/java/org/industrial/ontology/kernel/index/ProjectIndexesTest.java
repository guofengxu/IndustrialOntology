package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.index.BuiltInEntitiesIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesByEntityIndex;
import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsSignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectAxiomsSignatureIndex;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * {@link ProjectIndexes} wires every index of {@code kernel.api.index} exactly once (docs/01 §3.2 rule 3).
 */
public class ProjectIndexesTest {

    /**
     * Interfaces of {@code kernel.api.index} that are not looked up: markers, and the common super-interface of the
     * two built-in vocabularies, which would be ambiguous.
     */
    private static final Set<Class<?>> NOT_LOOKED_UP = Set.of(Index.class,
                                                              DependentIndex.class,
                                                              BuiltInEntitiesIndex.class);

    private ProjectIndexes.Builder builder;

    private ProjectIndexes indexes;

    @BeforeEach
    public void setUp() {
        builder = ProjectIndexes.builder(new OWLDataFactoryImpl());
        indexes = builder.build(mock(ClassHierarchyProvider.class),
                                mock(DictionaryManager.class),
                                mock(DeprecatedEntitiesIndex.class),
                                mock(DeprecatedEntitiesByEntityIndex.class));
    }

    @Test
    public void shouldRegisterAnImplementationOfEveryApiIndexInterface() throws Exception {
        var interfaces = apiIndexInterfaces();
        assertThat(interfaces.size(), is(greaterThan(55)));
        for(var type : interfaces) {
            assertThat(type.getName(), indexes.get(type), is(instanceOf(type)));
        }
    }

    @Test
    public void shouldAnswerSeveralInterfacesWithOneImplementation() {
        var axiomsByEntityReference = indexes.get(AxiomsByEntityReferenceIndex.class);
        assertThat(indexes.get(org.industrial.ontology.kernel.api.index.AxiomsByEntityReferenceIndex.class),
                   is(sameInstance(axiomsByEntityReference)));
        assertThat(indexes.get(OntologyAxiomsSignatureIndex.class), is(sameInstance(axiomsByEntityReference)));
        assertThat(indexes.get(ProjectAxiomsSignatureIndex.class), is(sameInstance(axiomsByEntityReference)));

        var ontologyAnnotations = indexes.get(OntologyAnnotationsIndex.class);
        assertThat(indexes.get(OntologyAnnotationsSignatureIndex.class), is(sameInstance(ontologyAnnotations)));
    }

    @Test
    public void shouldUpdateExactlyTheSeventeenPrimaryIndexes() throws Exception {
        assertThat(indexes.updatable(), hasSize(17));
        assertThat(builder.updatable(), is(indexes.updatable()));
        var missing = new ArrayList<String>();
        for(var index : registeredIndexes()) {
            if(index instanceof UpdatableIndex && !indexes.updatable().contains(index)) {
                missing.add(index.getClass().getSimpleName());
            }
        }
        assertThat(missing, is(empty()));
    }

    @Test
    public void shouldWireEveryDependencyToTheRegisteredInstance() throws Exception {
        var registered = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
        registered.addAll(registeredIndexes());
        var unregisteredDependencies = new ArrayList<String>();
        for(var index : registered) {
            if(index instanceof DependentIndex dependentIndex) {
                for(var dependency : dependentIndex.getDependencies()) {
                    if(!registered.contains(dependency)) {
                        unregisteredDependencies.add(index.getClass().getSimpleName() + " -> "
                                                             + dependency.getClass().getSimpleName());
                    }
                }
            }
        }
        assertThat(unregisteredDependencies, is(empty()));
    }

    @Test
    public void shouldRejectUnknownTypes() {
        assertThrows(IllegalArgumentException.class, () -> indexes.get(String.class));
    }

    @Test
    public void shouldBuildOnlyOnce() {
        assertThrows(IllegalStateException.class,
                     () -> builder.build(mock(ClassHierarchyProvider.class),
                                         mock(DictionaryManager.class),
                                         mock(DeprecatedEntitiesIndex.class),
                                         mock(DeprecatedEntitiesByEntityIndex.class)));
    }

    private List<Object> registeredIndexes() throws Exception {
        return apiIndexInterfaces().stream().map(indexes::get).distinct().collect(toList());
    }

    /**
     * Every interface of {@code kernel.api.index}, read from the wp-kernel-api classes directory or jar.
     */
    private static List<Class<?>> apiIndexInterfaces() throws IOException, URISyntaxException {
        var packagePath = Index.class.getPackageName().replace('.', '/');
        var location = Path.of(Index.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if(Files.isDirectory(location)) {
            return interfacesIn(location.resolve(packagePath));
        }
        try(var jar = FileSystems.newFileSystem(location, (ClassLoader) null)) {
            return interfacesIn(jar.getPath(packagePath));
        }
    }

    private static List<Class<?>> interfacesIn(Path packageDirectory) throws IOException {
        try(Stream<Path> files = Files.list(packageDirectory)) {
            return files.map(file -> file.getFileName().toString())
                        .filter(name -> name.endsWith(".class") && !name.contains("$"))
                        .map(name -> Index.class.getPackageName() + "." + name.substring(0, name.length() - 6))
                        .map(ProjectIndexesTest::load)
                        .filter(Class::isInterface)
                        .filter(type -> !NOT_LOOKED_UP.contains(type))
                        .sorted(Comparator.comparing(Class::getName))
                        .collect(toList());
        }
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }
}
