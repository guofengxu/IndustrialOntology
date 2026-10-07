package org.industrial.ontology.kernel.mansyntax.render;



import com.google.common.collect.Lists;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;
import java.util.List;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.ClassFrameRenderer}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 24/02/2014
 */
public class ClassFrameRenderer implements FrameRenderer<OWLClass> {

    @Nonnull
    private final AnnotationsSectionRenderer<OWLClass> annotationsSectionRenderer;

    @Nonnull
    private final ClassSubClassOfSectionRenderer subClassOfSectionRenderer;

    @Nonnull
    private final ClassEquivalentToSectionRenderer equivalentToSectionRenderer;

    @Nonnull
    private final ClassDisjointWithSectionRenderer disjointWithSectionRenderer;

    public ClassFrameRenderer(@Nonnull AnnotationsSectionRenderer<OWLClass> annotationsSectionRenderer,
                              @Nonnull ClassSubClassOfSectionRenderer subClassOfSectionRenderer,
                              @Nonnull ClassEquivalentToSectionRenderer equivalentToSectionRenderer,
                              @Nonnull ClassDisjointWithSectionRenderer disjointWithSectionRenderer) {
        this.annotationsSectionRenderer = annotationsSectionRenderer;
        this.subClassOfSectionRenderer = subClassOfSectionRenderer;
        this.equivalentToSectionRenderer = equivalentToSectionRenderer;
        this.disjointWithSectionRenderer = disjointWithSectionRenderer;
    }

    @Override
    public List<FrameSectionRenderer<OWLClass, ?, ?>> getSectionRenderers() {
        List<FrameSectionRenderer<OWLClass, ?, ?>> renderers = Lists.newArrayList();
        renderers.add(annotationsSectionRenderer);
        renderers.add(subClassOfSectionRenderer);
        renderers.add(equivalentToSectionRenderer);
        renderers.add(disjointWithSectionRenderer);
        return renderers;

    }
}
