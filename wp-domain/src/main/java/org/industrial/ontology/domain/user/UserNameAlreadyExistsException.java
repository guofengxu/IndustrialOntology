package org.industrial.ontology.domain.user;



import java.io.Serializable;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.UserNameAlreadyExistsException}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/06/2012
 */
public class UserNameAlreadyExistsException extends UserRegistrationException implements Serializable {

    private String username;

    private UserNameAlreadyExistsException() {
    }

    public UserNameAlreadyExistsException(String username) {
        super("User name already taken: " + username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
