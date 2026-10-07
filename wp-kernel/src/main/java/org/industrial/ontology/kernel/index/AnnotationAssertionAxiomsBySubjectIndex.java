package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsBySubjectIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public class AnnotationAssertionAxiomsBySubjectIndex implements org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLAnnotationSubject, OWLAnnotationAssertionAxiom> index;

    public AnnotationAssertionAxiomsBySubjectIndex() {
        index = AxiomMultimapIndex.create(OWLAnnotationAssertionAxiom.class,
                                          OWLAnnotationAssertionAxiom::getSubject);
    }

    @Override
    public Stream<OWLAnnotationAssertionAxiom> getAxiomsForSubject(@Nonnull OWLAnnotationSubject subject,
                                                                   @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(subject);
        checkNotNull(ontologyId);
        return index.getAxioms(subject, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
