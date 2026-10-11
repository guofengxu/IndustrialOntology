package org.industrial.ontology.api;

import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The wp-api beans with wp-app's auto-configurations, for the API tests. wp-server assembles the real application;
 * this one has its own data directory and adds stand-ins for the {@code /data/*} compatibility paths, whose
 * controllers come in later stages.
 */
@SpringBootApplication
public class ApiTestApplication {

    /**
     * A fresh data directory for each application context, in place of wp-server's {@code DataDirectoryConfiguration};
     * with it the project registry and the project services exist (S6).
     */
    @Bean
    DataDirectoryLayout dataDirectoryLayout() throws IOException {
        return new DataDirectoryLayout(Files.createTempDirectory("wp-api-data-"));
    }

    /**
     * {@code /data/*} and a path below {@code /download} (07 6-6): they only report who the caller is.
     */
    @RestController
    static class CompatibilityPathsController {

        @GetMapping({"/download/other", "/data/projects"})
        Map<String, String> caller(@Caller UserId caller) {
            return Map.of("caller", caller.getUserName());
        }

        /**
         * An asynchronous response, as a streamed download would be: the security chain runs again when the result is
         * dispatched.
         */
        @GetMapping("/data/async")
        Callable<Map<String, String>> asyncCaller() {
            return () -> Map.of("caller", SecurityContextHolder.getContext().getAuthentication().getName());
        }
    }
}
