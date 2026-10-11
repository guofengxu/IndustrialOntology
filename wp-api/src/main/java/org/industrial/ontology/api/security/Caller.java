package org.industrial.ontology.api.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller parameter of type {@code UserId} that receives the authenticated user of the request, whether a
 * Keycloak token, an API key or the local login authenticated it. Controllers pass it to the services, which check
 * its permissions (docs/01 §6).
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Caller {
}
