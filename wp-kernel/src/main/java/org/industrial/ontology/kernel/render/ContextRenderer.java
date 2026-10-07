package org.industrial.ontology.kernel.render;



import javax.annotation.Nonnull;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;

import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.ContextRenderer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21 Jul 2018
 */
public class ContextRenderer {

    private final RenderingManager renderingManager;

    public ContextRenderer(@Nonnull RenderingManager renderingManager) {
        this.renderingManager = renderingManager;
    }

    @Nonnull
    public OWLEntityData getEntityData(@Nonnull OWLEntity entity) {
        return renderingManager.getRendering(entity);
    }

    @Nonnull
    public OWLClassData getClassData(@Nonnull OWLClass cls) {
        return renderingManager.getClassData(cls);
    }

    @Nonnull
    public OWLObjectPropertyData getObjectPropertyData(@Nonnull OWLObjectProperty property) {
        return renderingManager.getObjectPropertyData(property);
    }

    @Nonnull
    public OWLDataPropertyData getDataPropertyData(@Nonnull OWLDataProperty property) {
        return renderingManager.getDataPropertyData(property);
    }

    @Nonnull
    public OWLAnnotationPropertyData getAnnotationPropertyData(@Nonnull OWLAnnotationProperty property) {
        return renderingManager.getAnnotationPropertyData(property);
    }

    @Nonnull
    public OWLNamedIndividualData getIndividualData(@Nonnull OWLNamedIndividual individual) {
        return renderingManager.getIndividualData(individual);
    }

    @Nonnull
    public OWLDatatypeData getDatatypeData(@Nonnull OWLDatatype datatype) {
        return renderingManager.getDatatypeData(datatype);
    }

    @Nonnull
    public OWLPrimitiveData getAnnotationValueData(@Nonnull OWLAnnotationValue value) {
        return renderingManager.getRendering(value);
    }
}
