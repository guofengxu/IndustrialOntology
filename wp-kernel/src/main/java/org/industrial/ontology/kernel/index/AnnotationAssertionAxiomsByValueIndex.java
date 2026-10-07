package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsByValueIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-13
 */
public class AnnotationAssertionAxiomsByValueIndex implements org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsByValueIndex, UpdatableIndex {

    private final AxiomMultimapIndex<OWLAnnotationValue, OWLAnnotationAssertionAxiom> index;

    public AnnotationAssertionAxiomsByValueIndex() {
        index = AxiomMultimapIndex.create(OWLAnnotationAssertionAxiom.class,
                                          AnnotationAssertionAxiomsByValueIndex::extractAnnotationValue);
    }

    @Nullable
    private static OWLAnnotationValue extractAnnotationValue(@Nonnull OWLAnnotationAssertionAxiom axiom) {
        var value = axiom.getValue();
        if(value instanceof OWLLiteral) {
            return null;
        }
        else {
            return value;
        }
    }

    @Nonnull
    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAxiomsByValue(@Nonnull OWLAnnotationValue value,
                                                                @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(value);
        checkNotNull(ontologyId);
        return index.getAxioms(value, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
