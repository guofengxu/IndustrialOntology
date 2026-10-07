package org.industrial.ontology.kernel.api.shortform;

import javax.annotation.Nonnull;
import java.util.Comparator;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ShortFormMatchPosition}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-13
 */
public record ShortFormMatchPosition(int start, int end) implements Comparable<ShortFormMatchPosition> {

    public static final Comparator<ShortFormMatchPosition> comparator = Comparator.comparing(ShortFormMatchPosition::getStart).thenComparing(ShortFormMatchPosition::getEnd);

    @Nonnull
    public static ShortFormMatchPosition get(int start, int end) {
        checkArgument(start <= end);
        checkArgument(0 <= start);
        return new ShortFormMatchPosition(start, end);
    }

    @Override
    public int compareTo(ShortFormMatchPosition o) {
        return comparator.compare(this, o);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
