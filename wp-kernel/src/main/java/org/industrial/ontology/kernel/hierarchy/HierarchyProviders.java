package org.industrial.ontology.kernel.hierarchy;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * The four entity hierarchies of one project, held by {@code ProjectContext} (docs/01 §2).
 */
public record HierarchyProviders(@Nonnull ClassHierarchyProvider classHierarchy,
                                 @Nonnull ObjectPropertyHierarchyProvider objectPropertyHierarchy,
                                 @Nonnull DataPropertyHierarchyProvider dataPropertyHierarchy,
                                 @Nonnull AnnotationPropertyHierarchyProvider annotationPropertyHierarchy) {

    public HierarchyProviders {
        Objects.requireNonNull(classHierarchy, "classHierarchy");
        Objects.requireNonNull(objectPropertyHierarchy, "objectPropertyHierarchy");
        Objects.requireNonNull(dataPropertyHierarchy, "dataPropertyHierarchy");
        Objects.requireNonNull(annotationPropertyHierarchy, "annotationPropertyHierarchy");
    }

    /**
     * Looks the hierarchies up by {@code HierarchyId}, as the hierarchy use cases do.
     */
    @Nonnull
    public HierarchyProviderMapper mapper() {
        return new HierarchyProviderMapper(classHierarchy,
                                           objectPropertyHierarchy,
                                           dataPropertyHierarchy,
                                           annotationPropertyHierarchy);
    }
}
