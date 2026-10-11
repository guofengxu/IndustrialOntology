package org.industrial.ontology.app.persistence;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.watch.persistence.WatchDocument;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.watches.WatchType;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.api.port.EntityFormRepository;
import org.industrial.ontology.kernel.api.port.EntityFormSelectorRepository;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettingsRepository;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Spring Data mapping of {@link MongoPersistenceAutoConfiguration}, without a server: Mongo clients connect
 * lazily, and the startup index check is switched off where no server is needed.
 */
class MongoPersistenceAutoConfigurationTest {

    private static final String PROJECT_ID = "2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class,
                                                     MongoDataAutoConfiguration.class,
                                                     MongoPersistenceAutoConfiguration.class))
            .withPropertyValues("spring.data.mongodb.uri=mongodb://localhost:1/webprotege",
                                "webprotege.mongo.ensure-indexes=false");

    private final OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl();

    @Test
    void shouldReplaceSpringBootsConverterWithOneWithoutTypeKey() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MappingMongoConverter.class);
            assertThat(context.getBean(MappingMongoConverter.class).getTypeMapper().isTypeKey("_class")).isFalse();
        });
    }

    @Test
    void shouldWriteNoClassFieldAndEntitiesInTheLegacyFormat() {
        contextRunner.run(context -> {
            var converter = context.getBean(MappingMongoConverter.class);
            var pizza = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Pizza"));
            var document = new Document();
            converter.write(new WatchDocument(null, PROJECT_ID, "alice", pizza, WatchType.BRANCH), document);

            assertThat(LegacyMongoSamples.canonical(document)).isEqualTo(
                    "{\"projectId\": \"" + PROJECT_ID + "\", \"userId\": \"alice\", "
                            + "\"entity\": {\"type\": \"Class\", \"iri\": \"http://example.org/pizza#Pizza\"}, "
                            + "\"type\": \"BRANCH\"}");
        });
    }

    @Test
    void shouldWriteEmbeddedIdsAsUnderscoreIdWithoutClassField() {
        contextRunner.run(context -> {
            var converter = context.getBean(MappingMongoConverter.class);
            var pizza = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Pizza"));
            var comment = new DiscussionThreadDocument.CommentDocument("c1", "alice", 1L, null, "Hi", "<p>Hi</p>");
            var document = new Document();
            converter.write(new DiscussionThreadDocument("t1", PROJECT_ID, pizza, Status.OPEN, List.of(comment)),
                            document);

            var stored = document.getList("comments", Document.class).get(0);
            assertThat(List.copyOf(stored.keySet())).containsExactly("_id", "createdBy", "createdAt", "body",
                                                                     "renderedBody");
            assertThat(stored.get("createdAt")).isEqualTo(1L);
            assertThat(document).doesNotContainKey("_class");
        });
    }

    @Test
    void shouldIgnoreMorphiasClassNameWhenReading() {
        contextRunner.run(context -> {
            var converter = context.getBean(MappingMongoConverter.class);
            var id = new ObjectId();
            var stored = new Document("_id", id)
                    .append("className", "edu.stanford.bmir.protege.web.server.watches.WatchRecord")
                    .append("projectId", PROJECT_ID)
                    .append("userId", "alice")
                    .append("entity", new Document("type", "NamedIndividual").append("iri", "http://example.org/i"))
                    .append("type", "ENTITY");

            var watch = converter.read(WatchDocument.class, stored);

            assertThat(watch).isEqualTo(new WatchDocument(id, PROJECT_ID, "alice",
                                                          dataFactory.getOWLNamedIndividual(
                                                                  IRI.create("http://example.org/i")),
                                                          WatchType.ENTITY));
        });
    }

    @Test
    void shouldProvideTheKernelPortsAndRepositories() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JacksonDocumentMapper.class)
                               .hasSingleBean(MongoIndexes.class)
                               .hasSingleBean(MongoMigration.class);
            assertThat(context.getBean(ProjectDetailsRepository.class))
                    .isSameAs(context.getBean(MongoProjectDetailsRepository.class));
            assertThat(context).hasSingleBean(PrefixDeclarationsStore.class)
                               .hasSingleBean(ProjectEntityCrudKitSettingsRepository.class)
                               .hasSingleBean(EntityDiscussionThreadRepository.class)
                               .hasSingleBean(EntityFormRepository.class)
                               .hasSingleBean(EntityFormSelectorRepository.class);
        });
    }

    @Test
    void shouldNotCheckIndexesAtStartupWhenSwitchedOff() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean("mongoIndexesOnStartup"));
    }

    /**
     * The index check runs by default, and a database that cannot be reached is logged without stopping startup.
     */
    @Test
    void shouldStartWhenTheStartupIndexCheckCannotReachTheDatabase() {
        contextRunner.withPropertyValues("webprotege.mongo.ensure-indexes=true",
                                         "spring.data.mongodb.uri=mongodb://localhost:1/webprotege"
                                                 + "?serverSelectionTimeoutMS=100")
                     .run(context -> assertThat(context).hasNotFailed()
                                                        .getBean("mongoIndexesOnStartup")
                                                        .isInstanceOf(SmartInitializingSingleton.class));
    }
}
