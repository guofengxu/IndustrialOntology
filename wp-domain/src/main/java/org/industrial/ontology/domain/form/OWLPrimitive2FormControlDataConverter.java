package org.industrial.ontology.domain.form;



import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.OWLPrimitive2FormControlDataConverter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-01
 */
public class OWLPrimitive2FormControlDataConverter {

    @Nonnull
    private final OWLEntity2FormControlDataVisitor entityVisitor;

    public OWLPrimitive2FormControlDataConverter(@Nonnull OWLEntity2FormControlDataVisitor entityVisitor) {
        this.entityVisitor = entityVisitor;
    }

    @Nonnull
    public PrimitiveFormControlData toFormControlData(@Nonnull OWLPrimitive primitive) {
        if(primitive instanceof OWLEntity) {
            return ((OWLEntity) primitive).accept(entityVisitor);
        }
        else if(primitive instanceof IRI) {
            return PrimitiveFormControlData.get((IRI) primitive);
        }
        else if(primitive instanceof OWLLiteral) {
            return PrimitiveFormControlData.get((OWLLiteral) primitive);
        }
        else if(primitive instanceof OWLAnonymousIndividual) {
            throw new RuntimeException("Anonymous individuals are not supported");
        }
        else {
            throw new RuntimeException("Missing case for primitive type " + primitive.getClass().getName());
        }
    }
}
