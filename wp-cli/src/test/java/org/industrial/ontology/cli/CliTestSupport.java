package org.industrial.ontology.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the command line as an operator would, with the database given as a Spring Boot argument.
 */
final class CliTestSupport {

    private CliTestSupport() {
    }

    static int run(String mongoUri, String... args) {
        var commandLine = new ArrayList<>(List.of(args));
        commandLine.add("--spring.data.mongodb.uri=" + mongoUri);
        var context = new SpringApplicationBuilder(WebProtegeCli.class).run(commandLine.toArray(String[]::new));
        return SpringApplication.exit(context);
    }

    /**
     * The output without picocli's ANSI styling.
     */
    static String plain(String output) {
        return output.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
