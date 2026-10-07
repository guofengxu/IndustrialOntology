package org.industrial.ontology.server;

import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Binds {@code webprotege.data-directory} (legacy {@code data.directory}) to the legacy on-disk layout and reports
 * its health. The directory is created on startup as the legacy server did; if that fails the server still starts
 * and the {@code dataDirectory} health component reports DOWN.
 */
@Configuration(proxyBeanMethods = false)
public class DataDirectoryConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DataDirectoryConfiguration.class);

    @Bean
    public DataDirectoryLayout dataDirectoryLayout(@Value("${webprotege.data-directory}") Path dataDirectory) {
        var layout = new DataDirectoryLayout(dataDirectory);
        try {
            Files.createDirectories(layout.getDataDirectory());
        }
        catch (IOException e) {
            logger.warn("Could not create data directory {}: {}", layout.getDataDirectory(), e.getMessage());
        }
        logger.info("Using data directory {}", layout.getDataDirectory());
        return layout;
    }

    @Bean
    public DataDirectoryHealthIndicator dataDirectoryHealthIndicator(DataDirectoryLayout dataDirectoryLayout) {
        return new DataDirectoryHealthIndicator(dataDirectoryLayout.getDataDirectory());
    }
}
