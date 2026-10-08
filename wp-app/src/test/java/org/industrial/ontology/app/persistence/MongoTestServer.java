package org.industrial.ontology.app.persistence;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import de.bwaldvogel.mongo.bson.Document;
import io.netty.channel.Channel;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

/**
 * The Mongo server of the persistence tests, started once per test JVM.
 * <p>
 * {@code -Dwp.test.mongo} chooses it:
 * <ul>
 *     <li>{@code auto} (the default): a {@code mongo:7.0} container (Testcontainers) where Docker runs, otherwise the
 *     in-memory server;</li>
 *     <li>{@code container}: always the container, failing without Docker; CI uses this so that a broken Docker
 *     setup cannot silently fall back;</li>
 *     <li>{@code memory}: <a href="https://github.com/bwaldvogel/mongo-java-server">mongo-java-server</a>, an
 *     in-memory implementation of the wire protocol that reports MongoDB 5.0. It runs the same tests without
 *     Docker, but it is not MongoDB: the container run in CI is the one that counts;</li>
 *     <li>a {@code mongodb://} connection string: an external server.</li>
 * </ul>
 * Every test class should use its own database ({@link #uniqueDatabase()}), since the server is shared.
 */
public final class MongoTestServer {

    public static final String MODE_PROPERTY = "wp.test.mongo";

    private static final String MONGO_IMAGE = "mongo:7.0";

    private static String baseUri;

    private static String backend;

    private MongoTestServer() {
    }

    /**
     * A connection string for the database on the shared server.
     */
    public static synchronized String uri(String database) {
        if (baseUri == null) {
            start();
        }
        return baseUri + "/" + database;
    }

    /**
     * A database name no other test uses.
     */
    public static String uniqueDatabase() {
        return "wp_test_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * What the tests run against, for messages.
     */
    public static synchronized String backend() {
        if (baseUri == null) {
            start();
        }
        return backend;
    }

    private static void start() {
        var mode = System.getProperty(MODE_PROPERTY, "auto");
        if (mode.startsWith("mongodb://")) {
            baseUri = mode.replaceAll("/+$", "");
            backend = "external server " + baseUri;
        } else if (mode.equals("container") || mode.equals("auto") && dockerAvailable()) {
            startContainer();
        } else if (mode.equals("memory") || mode.equals("auto")) {
            startInMemory();
        } else {
            throw new IllegalArgumentException(MODE_PROPERTY + " must be auto, container, memory or a mongodb:// "
                                                       + "connection string, not " + mode);
        }
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            return false;
        }
    }

    @SuppressWarnings("resource")
    private static void startContainer() {
        var container = new MongoDBContainer(DockerImageName.parse(MONGO_IMAGE));
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(container::stop, "mongo-test-container-stop"));
        baseUri = container.getConnectionString();
        backend = MONGO_IMAGE + " container";
    }

    private static void startInMemory() {
        var server = new MongoServer(new HelloMemoryBackend().version(ServerVersion.MONGO_5_0));
        var address = server.bind();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow, "mongo-test-memory-stop"));
        baseUri = "mongodb://" + address.getHostString() + ":" + address.getPort();
        backend = "in-memory mongo-java-server";
    }

    /**
     * mongo-java-server 1.47 does not know the {@code hello} command, which Spring Boot's Mongo health indicator
     * sends; MongoDB answers it like {@code isMaster} (its older name), and so does this backend.
     */
    private static final class HelloMemoryBackend extends MemoryBackend {

        @Override
        public Document handleCommand(Channel channel, String databaseName, String command, Document query) {
            var name = command.equalsIgnoreCase("hello") ? "isMaster" : command;
            return super.handleCommand(channel, databaseName, name, query);
        }
    }
}
