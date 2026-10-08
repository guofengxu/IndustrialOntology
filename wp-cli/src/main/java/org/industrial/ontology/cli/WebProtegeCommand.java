package org.industrial.ontology.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/**
 * The {@code wp-cli} command; the administration commands are its subcommands (docs/01 §8). S4 adds
 * {@code migrate-mongo}; {@code create-admin}, {@code generate-api-key}, {@code rebuild-permissions},
 * {@code set-permissions} and {@code reindex-lucene} follow in S5, S6 and S10.
 */
@Component
@Command(name = "wp-cli",
         mixinStandardHelpOptions = true,
         description = "Administration commands for the IndustrialOntology database and data directory.",
         subcommands = {MigrateMongoCommand.class})
public class WebProtegeCommand implements Runnable {

    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing command");
    }
}
