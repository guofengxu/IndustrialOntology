package org.industrial.ontology.kernel.form;



import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.domain.frame.PlainEntityFrame;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityFrameMapperFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-03-26
 */
public class EntityFrameMapperFactory {

    @Nonnull
    private final ClassAssertionAxiomsByClassIndex classAssertionAxiomsIndex;

    @Nonnull
    private ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private RenderingManager renderingManager;

    @Nonnull
    private final MatcherFactory matcherFactory;

    public EntityFrameMapperFactory(@Nonnull ClassAssertionAxiomsByClassIndex classAssertionAxiomsIndex,
                                    @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                    @Nonnull RenderingManager renderingManager,
                                    @Nonnull MatcherFactory matcherFactory) {
        this.classAssertionAxiomsIndex = checkNotNull(classAssertionAxiomsIndex);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.renderingManager = checkNotNull(renderingManager);
        this.matcherFactory = checkNotNull(matcherFactory);
    }

    public EntityFrameMapper create(@Nonnull PlainEntityFrame entityFrame) {
        return new EntityFrameMapper(entityFrame, projectOntologiesIndex, classAssertionAxiomsIndex, renderingManager,
                                     matcherFactory);
    }
}
