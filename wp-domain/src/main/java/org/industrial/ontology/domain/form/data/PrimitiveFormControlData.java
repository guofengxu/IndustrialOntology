package org.industrial.ontology.domain.form.data;



import org.industrial.ontology.domain.core.DataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLPrimitive;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.PrimitiveFormControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public interface PrimitiveFormControlData {

    static PrimitiveFormControlData get(OWLEntity entity) {
        return EntityFormControlData.get(entity);
    }

    static PrimitiveFormControlData get(IRI iri) {
        return IriFormControlData.get(iri);
    }

    static PrimitiveFormControlData get(OWLLiteral literal) {
        return LiteralFormControlData.get(literal);
    }

    static PrimitiveFormControlData get(String text) {
        return LiteralFormControlData.get(DataFactory.getOWLLiteral(text));
    }

    static PrimitiveFormControlData get(double value) {
        return LiteralFormControlData.get(DataFactory.getOWLLiteral(value));
    }

    static PrimitiveFormControlData get(boolean value) {
        return LiteralFormControlData.get(DataFactory.getOWLLiteral(value));
    }

    @Nonnull
    Optional<OWLEntity> asEntity();

    @Nonnull
    Optional<IRI> asIri();

    @Nonnull
    Optional<OWLLiteral> asLiteral();

    @Nonnull
    OWLPrimitive getPrimitive();
}
