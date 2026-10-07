package org.industrial.ontology.kernel.msg;



import org.industrial.ontology.kernel.render.RenderingManager;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.msg.MessageFormatter}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 19 Dec 2017
 */
public class MessageFormatter {

    @Nonnull
    private final RenderingManager renderingManager;

    public MessageFormatter(@Nonnull RenderingManager renderingManager) {
        this.renderingManager = checkNotNull(renderingManager);
    }

    public String format(@Nonnull String template,
                         @Nonnull Object ... objects) {
        return OWLMessageFormatter.formatMessage(checkNotNull(template), renderingManager, checkNotNull(objects));
    }
}
