package org.industrial.ontology.domain.frame;



import com.google.common.collect.ImmutableSet;
import javax.annotation.Nonnull;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;

import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLLiteralData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.FrameComponentRenderer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-03-31
 */
public interface FrameComponentRenderer {

    @Nonnull
    OWLClassData getRendering(@Nonnull OWLClass cls);

    @Nonnull
    OWLObjectPropertyData getRendering(@Nonnull OWLObjectProperty property);

    @Nonnull
    OWLDataPropertyData getRendering(@Nonnull OWLDataProperty property);

    @Nonnull
    OWLAnnotationPropertyData getRendering(@Nonnull OWLAnnotationProperty property);

    @Nonnull
    OWLNamedIndividualData getRendering(@Nonnull OWLNamedIndividual individual);

    @Nonnull
    OWLDatatypeData getRendering(@Nonnull OWLDatatype datatype);

    @Nonnull
    OWLLiteralData getRendering(@Nonnull OWLLiteral literal);

    @Nonnull
    OWLPrimitiveData getRendering(@Nonnull OWLAnnotationValue annotationValue);

    @Nonnull
    ImmutableSet<OWLEntityData> getRendering(@Nonnull IRI iri);

    @Nonnull
    OWLEntityData getEntityRendering(@Nonnull OWLEntity entity);
}
