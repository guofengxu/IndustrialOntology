package org.industrial.ontology.domain.user;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.UserRegistrationException}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/06/2012
 */
public class UserRegistrationException extends RuntimeException {

    public UserRegistrationException() {
    }

    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserRegistrationException(Throwable cause) {
        super(cause);
    }
}
