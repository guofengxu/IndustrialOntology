package org.industrial.ontology.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The administration command line (docs/01 §8): a non-web Spring Boot application with wp-app's persistence, which
 * runs one picocli command ({@link WebProtegeCommand}) and exits with its exit code.
 * <p>
 * It reads the same {@code WP_MONGODB_URI} as the server. Commands see the application context, so they use the
 * same repositories as the server does.
 */
@SpringBootApplication
public class WebProtegeCli {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(WebProtegeCli.class, args)));
    }
}
