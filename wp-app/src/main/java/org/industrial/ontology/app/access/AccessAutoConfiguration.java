package org.industrial.ontology.app.access;

import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.admin.ApplicationSettingsService;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.apikey.persistence.UserApiKeyRepository;
import org.industrial.ontology.app.persistence.MongoPersistenceAutoConfiguration;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

/**
 * Who the caller is and what it may do (docs/01 §5.1, §6; stage S5): the {@link AccessManager} on the
 * {@code RoleAssignments} collection, and the user, API key and application settings services.
 * <p>
 * An auto-configuration, like the persistence it builds on, so that wp-cli, which scans only its own package, gets
 * the same services as the server. wp-api contributes the {@link ExternalRoles} of the request (the Keycloak admin
 * realm role); without it there are none.
 */
@AutoConfiguration(after = MongoPersistenceAutoConfiguration.class)
public class AccessAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RoleOracle roleOracle() {
        return RoleOracle.get();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExternalRoles externalRoles() {
        return ExternalRoles.NONE;
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessManager accessManager(RoleOracle roleOracle,
                                       RoleAssignmentRepository roleAssignmentRepository,
                                       ExternalRoles externalRoles) {
        return new MongoAccessManager(roleOracle, roleAssignmentRepository, externalRoles);
    }

    /**
     * BCrypt for the local fallback login (docs/01 §6); legacy MD5 digests are not migrated.
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService userService(UserRecordRepository userRecordRepository,
                                   AccessManager accessManager,
                                   PasswordEncoder passwordEncoder) {
        return new UserService(userRecordRepository, accessManager, passwordEncoder);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiKeyService apiKeyService(UserApiKeyRepository userApiKeyRepository, AccessManager accessManager) {
        return new ApiKeyService(userApiKeyRepository, accessManager, Clock.systemUTC());
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationSettingsService applicationSettingsService(AccessManager accessManager,
                                                                 ApplicationPreferencesRepository preferences) {
        return new ApplicationSettingsService(accessManager, preferences);
    }
}
