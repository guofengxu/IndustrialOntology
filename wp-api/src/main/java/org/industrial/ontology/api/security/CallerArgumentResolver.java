package org.industrial.ontology.api.security;

import org.industrial.ontology.domain.core.UserId;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves {@link Caller @Caller} {@code UserId} parameters from the security context. Every authentication of the
 * chain names its user the same way ({@code Authentication.getName()}): the {@code preferred_username} of a token,
 * the owner of an API key.
 */
final class CallerArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Caller.class) && UserId.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer container,
                                  NativeWebRequest request,
                                  WebDataBinderFactory binderFactory) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            // The chain lets no anonymous request reach a controller with a caller; this is a configuration error.
            throw new AuthenticationCredentialsNotFoundException("The request is not authenticated");
        }
        return UserId.getUserId(authentication.getName());
    }
}
