package org.industrial.ontology.kernel.frame;

import javax.annotation.Nonnull;
import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainObjectPropertyFrame;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.FrameUpdate}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
public record FrameUpdate(PlainEntityFrame fromFrame, PlainEntityFrame toFrame) {

    public FrameUpdate {
        Objects.requireNonNull(fromFrame, "Null fromFrame");
        Objects.requireNonNull(toFrame, "Null toFrame");
    }

    public static FrameUpdate get(PlainEntityFrame from, PlainEntityFrame to) {
        if (!from.getClass().equals(to.getClass())) {
            throw new RuntimeException("Frames must be of the same type");
        }
        if (from instanceof PlainClassFrame) {
            return get((PlainClassFrame) from, (PlainClassFrame) to);
        }
        if (from instanceof PlainNamedIndividualFrame) {
            return get((PlainNamedIndividualFrame) from, (PlainNamedIndividualFrame) to);
        }
        if (from instanceof PlainObjectPropertyFrame) {
            return get((PlainObjectPropertyFrame) from, (PlainObjectPropertyFrame) to);
        }
        if (from instanceof PlainDataPropertyFrame) {
            return get((PlainDataPropertyFrame) from, (PlainDataPropertyFrame) to);
        }
        if (from instanceof PlainAnnotationPropertyFrame) {
            return get((PlainAnnotationPropertyFrame) from, (PlainAnnotationPropertyFrame) to);
        }
        throw new RuntimeException("Unknown frame type: " + from);
    }

    public static FrameUpdate get(@Nonnull PlainClassFrame fromFrame, @Nonnull PlainClassFrame toFrame) {
        return new FrameUpdate(fromFrame, toFrame);
    }

    public static FrameUpdate get(@Nonnull PlainNamedIndividualFrame fromFrame, @Nonnull PlainNamedIndividualFrame toFrame) {
        return new FrameUpdate(fromFrame, toFrame);
    }

    public static FrameUpdate get(@Nonnull PlainObjectPropertyFrame fromFrame, @Nonnull PlainObjectPropertyFrame toFrame) {
        return new FrameUpdate(fromFrame, toFrame);
    }

    public static FrameUpdate get(@Nonnull PlainDataPropertyFrame fromFrame, @Nonnull PlainDataPropertyFrame toFrame) {
        return new FrameUpdate(fromFrame, toFrame);
    }

    public static FrameUpdate get(@Nonnull PlainAnnotationPropertyFrame fromFrame, @Nonnull PlainAnnotationPropertyFrame toFrame) {
        return new FrameUpdate(fromFrame, toFrame);
    }

    public PlainEntityFrame getFromFrame() {
        return fromFrame;
    }

    public PlainEntityFrame getToFrame() {
        return toFrame;
    }
}
