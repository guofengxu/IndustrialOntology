package org.industrial.ontology.api;

import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * The wp-api beans with wp-app's auto-configurations, for the security tests. wp-server assembles the real
 * application; this one adds stand-ins for the compatibility paths, whose controllers come in later stages.
 */
@SpringBootApplication
public class ApiTestApplication {

    /**
     * {@code /download} and {@code /data/*} (07 6-6): they only report who the caller is.
     */
    @RestController
    static class CompatibilityPathsController {

        @GetMapping({"/download", "/download/other", "/data/projects"})
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
