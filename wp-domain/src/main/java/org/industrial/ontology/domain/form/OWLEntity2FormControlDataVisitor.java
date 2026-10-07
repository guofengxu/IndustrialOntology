package org.industrial.ontology.domain.form;



import javax.annotation.Nonnull;
import org.industrial.ontology.domain.form.data.EntityFormControlData;

import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.OWLEntity2FormControlDataVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-18
 */
public class OWLEntity2FormControlDataVisitor implements OWLEntityVisitorEx<PrimitiveFormControlData> {

    public OWLEntity2FormControlDataVisitor() {
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLClass data) {
        return EntityFormControlData.get(data);
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLObjectProperty data) {
        return EntityFormControlData.get(data);
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLDataProperty data) {
        return EntityFormControlData.get(data);
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLAnnotationProperty data) {
        return EntityFormControlData.get(data);
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLNamedIndividual data) {
        return EntityFormControlData.get(data);
    }

    @Nonnull
    @Override
    public PrimitiveFormControlData visit(@Nonnull OWLDatatype data) {
        return EntityFormControlData.get(data);
    }
}
