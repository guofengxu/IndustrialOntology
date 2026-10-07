package org.industrial.ontology.domain.hierarchy;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.hierarchy.HierarchyId}.
 * <p>
 * Author: Matthew Horridge<br> Stanford University<br> Bio-Medical Informatics Research Group<br> Date: 22/03/2013
 */
public record HierarchyId(@Nonnull String id) {

    public HierarchyId {
        Objects.requireNonNull(id, "Null id");
    }

    public static final HierarchyId CLASS_HIERARCHY = get("Class");

    public static final HierarchyId OBJECT_PROPERTY_HIERARCHY = get("ObjectProperty");

    public static final HierarchyId DATA_PROPERTY_HIERARCHY = get("DataProperty");

    public static final HierarchyId ANNOTATION_PROPERTY_HIERARCHY = get("AnnotationProperty");

    @Nonnull
    public static HierarchyId get(@Nonnull String id) {
        return new HierarchyId(checkNotNull(id));
    }

    @Nonnull
    public String getId() {
        return id;
    }
}
