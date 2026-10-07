package org.industrial.ontology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the IndustrialOntology server.
 *
 * <p>Lives in the package root so component scanning covers every module
 * ({@code org.industrial.ontology.api}, {@code .app}, {@code .integration}, ...) without
 * listing them explicitly.
 */
@SpringBootApplication
public class IndustrialOntologyApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustrialOntologyApplication.class, args);
    }
}
