package org.industrial.ontology.app.frame;

import org.industrial.ontology.domain.frame.EntityFrame;
import org.industrial.ontology.domain.frame.PlainEntityFrame;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An entity's frame in the two forms that docs/02 §4.3 returns: plain, which the client edits and sends back, and
 * rendered with display names and ordered property values, which it shows.
 */
public record EntityFrameView(@Nonnull PlainEntityFrame frame, @Nonnull EntityFrame<?> rendered) {

    public EntityFrameView {
        checkNotNull(frame);
        checkNotNull(rendered);
    }
}
