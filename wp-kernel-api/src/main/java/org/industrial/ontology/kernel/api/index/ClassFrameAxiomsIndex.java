package org.industrial.ontology.kernel.api.index;



import javax.annotation.Nonnull;

import java.util.Set;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.ClassFrameAxiomsIndex}.
 */
public interface ClassFrameAxiomsIndex {

    /**
     * Gets the axioms that constitute the class frame for the specified class.
     * @param subject The subject of the class frame
     * @param annotationsTreatment Specifies whether or not annotations should be included.
     * @return The axioms that make up the class frame for the specified class
     */
    @Nonnull
    Set<OWLAxiom> getFrameAxioms(@Nonnull OWLClass subject,
                                 @Nonnull AnnotationsTreatment annotationsTreatment);

    enum AnnotationsTreatment {
        INCLUDE_ANNOTATIONS,
        EXCLUDE_ANNOTATIONS
    }
}
