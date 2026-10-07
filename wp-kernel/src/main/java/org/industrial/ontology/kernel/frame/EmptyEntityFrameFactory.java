package org.industrial.ontology.kernel.frame;



import org.industrial.ontology.kernel.render.ContextRenderer;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainClassFrame;

import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainObjectPropertyFrame;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.EmptyEntityFrameFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-15
 */
public class EmptyEntityFrameFactory {

    @Nonnull
    private final ContextRenderer renderingManager;

    public EmptyEntityFrameFactory(@Nonnull ContextRenderer renderingManager) {
        this.renderingManager = checkNotNull(renderingManager);
    }

    public PlainEntityFrame getEmptyEntityFrame(OWLEntity entity) {
        return entity.accept(new OWLEntityVisitorEx<PlainEntityFrame>() {
            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLClass cls) {
                return PlainClassFrame.empty(cls);
            }

            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLObjectProperty property) {
                return PlainObjectPropertyFrame.empty(property);
            }

            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLDataProperty property) {
                return PlainDataPropertyFrame.empty(property);
            }

            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLNamedIndividual individual) {
                return PlainNamedIndividualFrame.empty(individual);
            }

            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLDatatype datatype) {
                throw new UnsupportedOperationException();
            }

            @Nonnull
            @Override
            public PlainEntityFrame visit(@Nonnull OWLAnnotationProperty property) {
                return PlainAnnotationPropertyFrame.empty(property);
            }
        });
    }
}
