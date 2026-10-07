package org.industrial.ontology.domain.app;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.app.StatusCodes}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 28 Mar 2018
 */
public class StatusCodes {

    /**
     * The shared client/server code has been updated.  The client needs to reload WebProtege.
     * This is intended to be sent to the client when there is a {@link SerializationException}
     * on the server that is caused by the client using outdated .rpc files.
     */
    public static final int UPDATED = 590;
}
