package org.industrial.ontology.kernel.hierarchy;



import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.industrial.ontology.kernel.api.hierarchy.AnnotationPropertyHierarchyProvider;
import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.hierarchy.DataPropertyHierarchyProvider;
import org.industrial.ontology.kernel.api.hierarchy.HierarchyProvider;
import org.industrial.ontology.kernel.api.hierarchy.ObjectPropertyHierarchyProvider;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.HierarchyProviderMapper}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 4 Dec 2017
 */
public class HierarchyProviderMapper {

    private final Map<HierarchyId, HierarchyProvider<? extends OWLEntity>> map = new HashMap<>();

    public HierarchyProviderMapper(@Nonnull ClassHierarchyProvider classHierarchyProvider,
                                   @Nonnull ObjectPropertyHierarchyProvider objectPropertyHierarchyProvider,
                                   @Nonnull DataPropertyHierarchyProvider dataPropertyHierarchyProvider,
                                   @Nonnull AnnotationPropertyHierarchyProvider annotationPropertyHierarchyProvider) {
        map.put(HierarchyId.CLASS_HIERARCHY, classHierarchyProvider);
        map.put(HierarchyId.OBJECT_PROPERTY_HIERARCHY, objectPropertyHierarchyProvider);
        map.put(HierarchyId.DATA_PROPERTY_HIERARCHY, dataPropertyHierarchyProvider);
        map.put(HierarchyId.ANNOTATION_PROPERTY_HIERARCHY, annotationPropertyHierarchyProvider);
    }

    @SuppressWarnings("unchecked")
    public Optional<HierarchyProvider<OWLEntity>> getHierarchyProvider(@Nonnull HierarchyId hierarchyId) {
        HierarchyProvider<OWLEntity> hierarchyProvider = (HierarchyProvider<OWLEntity>) map.get(hierarchyId);
        return Optional.ofNullable(hierarchyProvider);
    }
}
