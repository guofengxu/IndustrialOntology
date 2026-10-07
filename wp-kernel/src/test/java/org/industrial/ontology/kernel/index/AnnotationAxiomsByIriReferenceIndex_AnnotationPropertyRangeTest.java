package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyRangeAxiomImpl;
import java.util.Collections;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationAxiomsByIriReferenceIndexImpl_AnnotationPropertyRange_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-08
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnnotationAxiomsByIriReferenceIndex_AnnotationPropertyRangeTest {

    private AnnotationAxiomsByIriReferenceIndex impl;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private IRI rangeIri, otherRangeIri;

    private OWLAnnotationPropertyRangeAxiom annotationPropertyRangeAxiom, otherAnnotationPropertyRangeAxiom;

    @Mock
    private OWLAnnotationProperty property;

    @Mock
    private OWLAnnotation axiomAnnotation;

    @Mock
    private IRI axiomAnnotationValue;

    @BeforeEach
    public void setUp() {
        annotationPropertyRangeAxiom = new OWLAnnotationPropertyRangeAxiomImpl(property, rangeIri, axiomAnnotations());
        otherAnnotationPropertyRangeAxiom = new OWLAnnotationPropertyRangeAxiomImpl(property, otherRangeIri, axiomAnnotations());
        impl = new AnnotationAxiomsByIriReferenceIndex();
        impl.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyId, annotationPropertyRangeAxiom)));
    }

    private Set<OWLAnnotation> axiomAnnotations() {
        when(axiomAnnotation.getValue()).thenReturn(axiomAnnotationValue);
        return Collections.singleton(axiomAnnotation);
    }

    @Test
    public void shouldGetAnnotationPropertyRangeAxiomByRangeIri() {
        var axioms = impl.getReferencingAxioms(rangeIri, ontologyId).collect(toSet());
        assertThat(axioms, hasItem(annotationPropertyRangeAxiom));
    }

    @Test
    public void shouldGetAnnotationPropertyRangeAxiomByAxiomAnnotationValue() {
        var axioms = impl.getReferencingAxioms(axiomAnnotationValue, ontologyId).collect(toSet());
        assertThat(axioms, hasItem(annotationPropertyRangeAxiom));
    }

    @Test
    public void shouldHandleAddAnnotationPropertyRangeAxiom() {
        var addAxiom = AddAxiomChange.of(ontologyId, otherAnnotationPropertyRangeAxiom);
        impl.applyChanges(ImmutableList.of(addAxiom));
        var axioms = impl.getReferencingAxioms(otherRangeIri, ontologyId).collect(toSet());
        assertThat(axioms, hasItems(otherAnnotationPropertyRangeAxiom));
    }

    @Test
    public void shouldHandleRemoveAnnotationPropertyRangeAxiom() {
        var removeAxiom = RemoveAxiomChange.of(ontologyId, annotationPropertyRangeAxiom);
        impl.applyChanges(ImmutableList.of(removeAxiom));
        var axioms = impl.getReferencingAxioms(rangeIri, ontologyId).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }
}
