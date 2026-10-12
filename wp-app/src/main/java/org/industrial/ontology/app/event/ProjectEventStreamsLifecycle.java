package org.industrial.ontology.app.event;

import org.springframework.context.SmartLifecycle;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ends every event stream when the application starts to shut down (docs/01 §7). An event stream is a request that
 * never finishes by itself, so the web server's graceful shutdown, which waits for active requests, would otherwise
 * wait out its whole timeout. This lifecycle stops in the default phase, before the graceful shutdown phase of
 * Spring Boot's web server ({@code SmartLifecycle.DEFAULT_PHASE - 1024}); clients reconnect to the next instance from
 * the last id they received.
 */
public class ProjectEventStreamsLifecycle implements SmartLifecycle {

    private final ProjectEventBroadcaster broadcaster;

    private volatile boolean running;

    public ProjectEventStreamsLifecycle(@Nonnull ProjectEventBroadcaster broadcaster) {
        this.broadcaster = checkNotNull(broadcaster);
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        broadcaster.close();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return DEFAULT_PHASE;
    }
}
