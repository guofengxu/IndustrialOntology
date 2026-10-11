package org.industrial.ontology.cli;

import org.industrial.ontology.app.persistence.LegacyCollection;
import org.industrial.ontology.app.persistence.MongoIndexes;
import org.industrial.ontology.app.persistence.MongoMigration;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * {@code wp-cli migrate-mongo [--dry-run]}: prepares a legacy {@code webprotege} database for the ported server
 * ({@link MongoMigration}, docs/01 §5.3). It removes Morphia's {@code className} fields, creates the missing legacy
 * indexes and reports the documents whose embedded entity cannot be read. Running it again changes nothing more.
 * <p>
 * Exit code {@value #PROBLEMS_FOUND} means it ran, but some documents or indexes need attention; they are listed.
 */
@Component
@Command(name = "migrate-mongo",
         mixinStandardHelpOptions = true,
         description = {
                 "Prepares a legacy WebProtege database: removes Morphia's className fields, creates the missing "
                         + "legacy indexes and reports documents whose embedded entity cannot be read.",
                 "Safe to run again. Stop the server first.",
                 "Exit codes: 0 done, 3 done but problems are listed, 1 failed, 2 wrong arguments."})
public class MigrateMongoCommand implements Callable<Integer> {

    public static final int PROBLEMS_FOUND = 3;

    private final MongoMigration migration;

    @Spec
    private CommandSpec spec;

    @Option(names = "--dry-run", description = "Report what would be done without changing the database.")
    private boolean dryRun;

    public MigrateMongoCommand(MongoMigration migration) {
        this.migration = migration;
    }

    @Override
    public Integer call() {
        var report = migration.run(dryRun);
        print(report, spec.commandLine().getOut());
        return report.hasProblems() ? PROBLEMS_FOUND : 0;
    }

    private static void print(MongoMigration.Report report, PrintWriter out) {
        out.println(report.dryRun() ? "migrate-mongo: dry run, nothing is changed" : "migrate-mongo");

        var removals = Stream.of(LegacyCollection.values())
                             .filter(collection -> report.classNameRemovals().getOrDefault(collection, 0L) > 0)
                             .toList();
        out.println(report.dryRun() ? "Documents with a className field:" : "className removed from:");
        if (removals.isEmpty()) {
            out.println("  none");
        }
        removals.forEach(collection -> out.printf("  %s: %d%n", collection.collectionName(),
                                                  report.classNameRemovals().get(collection)));

        out.println("Indexes:");
        for (var result : report.indexes()) {
            if (result.outcome() == MongoIndexes.Outcome.PRESENT) {
                continue;
            }
            out.printf("  %-8s %s %s%s%s%n",
                       result.outcome().name().toLowerCase(),
                       result.collection().collectionName(),
                       result.index().keys(),
                       result.index().unique() ? " unique" : "",
                       result.problem() == null ? "" : ": " + result.problem());
        }
        var present = report.indexes().stream().filter(result -> result.outcome() == MongoIndexes.Outcome.PRESENT)
                            .count();
        out.printf("  %d of %d present before this run%n", present, report.indexes().size());

        out.println("Documents whose entity cannot be read:");
        if (report.anomalies().isEmpty()) {
            out.println("  none");
        }
        report.anomalies().forEach(anomaly -> out.printf("  %s %s, %s: %s%n",
                                                         anomaly.collection().collectionName(),
                                                         anomaly.documentId(),
                                                         anomaly.field(),
                                                         anomaly.problem()));
        out.println(report.hasProblems() ? "Problems found, see above." : "Done.");
        out.flush();
    }
}
