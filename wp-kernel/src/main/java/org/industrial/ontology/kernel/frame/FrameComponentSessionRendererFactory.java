package org.industrial.ontology.kernel.frame;



import org.industrial.ontology.domain.frame.FrameComponentRenderer;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.FrameComponentSessionRendererFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-01
 */
public class FrameComponentSessionRendererFactory {

    @Nonnull
    private final FrameComponentRenderer frameComponentRenderer;

    public FrameComponentSessionRendererFactory(@Nonnull FrameComponentRenderer frameComponentRenderer) {
        this.frameComponentRenderer = checkNotNull(frameComponentRenderer);
    }

    @Nonnull
    public FrameComponentSessionRenderer create() {
        return new FrameComponentSessionRenderer(frameComponentRenderer);
    }
}
