package org.industrial.ontology.app.event;

import org.industrial.ontology.kernel.event.EventBatch;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Where a subscription sends a project's events (docs/02 §5); wp-api implements it with a server-sent events emitter.
 * A subscription calls its sink from one thread at a time, never from the thread that posted the events nor from the
 * heartbeat thread, so the sink may block while its client is slow.
 */
public interface ProjectEventSink {

    /**
     * Sends the batch as one message whose id is the epoch and the batch's tag. Batches arrive in tag order, each
     * once.
     *
     * @param epoch the epoch of the project's tags ({@link EventPosition})
     * @throws IOException if the client has gone; the subscription is then closed
     */
    void send(@Nonnull String epoch, @Nonnull EventBatch batch) throws IOException;

    /**
     * Sends a keep-alive message that the client ignores.
     *
     * @throws IOException if the client has gone; the subscription is then closed
     */
    void ping() throws IOException;

    /**
     * Whether the credentials that opened the stream are still good, for example a token that has not expired or an
     * API key that has not been deleted. Checked with the user's permission; the subscription is closed when not.
     */
    default boolean credentialsValid() {
        return true;
    }

    /**
     * Ends the stream. Called once, when the subscription is closed for any reason but the client's own disconnect
     * (timeout, lost permission or credentials, a closed project, a client too slow, shutdown); the client may
     * reconnect from the last id it received.
     */
    void close();
}
