package org.industrial.ontology.app.user;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.SetEmailAddressResult.Result}: what
 * {@link UserService#setEmailAddress} did.
 */
public enum EmailAddressChange {

    ADDRESS_CHANGED,

    /** The user already had the address. */
    ADDRESS_UNCHANGED,

    /** Another user has the address; nothing changed. */
    ADDRESS_ALREADY_EXISTS
}
