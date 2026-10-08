package org.industrial.ontology.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Runs the command named on the command line, with commands and their options created by Spring (picocli's Spring
 * factory), and keeps its exit code for {@link org.springframework.boot.SpringApplication#exit}.
 * <p>
 * Arguments of the form {@code --name.with.dots=value} are Spring Boot properties, for example
 * {@code --spring.data.mongodb.uri=mongodb://host/webprotege}; Spring Boot reads them and they are not passed to the
 * command. Command options never contain a dot.
 * <p>
 * Exit codes: 0 success; 1 the command failed; 2 the command line was wrong; a command may define more (see
 * {@link MigrateMongoCommand}).
 */
@Component
public class CommandLineCliRunner implements CommandLineRunner, ExitCodeGenerator {

    private static final Pattern SPRING_PROPERTY = Pattern.compile("--[\\w-]+(\\.[\\w-]+)+=.*");

    private final WebProtegeCommand command;

    private final CommandLine.IFactory factory;

    private int exitCode;

    public CommandLineCliRunner(WebProtegeCommand command, CommandLine.IFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    @Override
    public void run(String... args) {
        var commandArgs = Arrays.stream(args).filter(arg -> !SPRING_PROPERTY.matcher(arg).matches())
                                .toArray(String[]::new);
        exitCode = new CommandLine(command, factory).execute(commandArgs);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
