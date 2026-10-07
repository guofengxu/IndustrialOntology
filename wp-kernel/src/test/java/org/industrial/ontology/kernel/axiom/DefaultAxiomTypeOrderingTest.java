package org.industrial.ontology.kernel.axiom;



import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.AxiomType;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.CLASS_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATATYPE_DEFINITION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.DECLARATION;
import static org.semanticweb.owlapi.model.AxiomType.DIFFERENT_INDIVIDUALS;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_UNION;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.HAS_KEY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SAME_INDIVIDUAL;
import static org.semanticweb.owlapi.model.AxiomType.SUBCLASS_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_ANNOTATION_PROPERTY_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_PROPERTY_CHAIN_OF;
import static org.semanticweb.owlapi.model.AxiomType.SWRL_RULE;
import static org.semanticweb.owlapi.model.AxiomType.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.TRANSITIVE_OBJECT_PROPERTY;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.axiom.DefaultAxiomTypeOrdering_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 03/02/15
 */
public class DefaultAxiomTypeOrderingTest {


    private List<AxiomType<?>> defaultAxiomTypes = DefaultAxiomTypeOrdering.get();


    @Test
    public void shouldContain_OWLDeclarationAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DECLARATION));

    }


    @Test
    public void shouldContain_OWLSubClassOfAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SUBCLASS_OF));

    }


    @Test
    public void shouldContain_OWLNegativeObjectPropertyAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(NEGATIVE_OBJECT_PROPERTY_ASSERTION));

    }


    @Test
    public void shouldContain_OWLAsymmetricObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(ASYMMETRIC_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLReflexiveObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(REFLEXIVE_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLDisjointClassesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DISJOINT_CLASSES));

    }


    @Test
    public void shouldContain_OWLDataPropertyDomainAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DATA_PROPERTY_DOMAIN));

    }


    @Test
    public void shouldContain_OWLObjectPropertyDomainAxiom() {
        assertThat(defaultAxiomTypes, hasItem(OBJECT_PROPERTY_DOMAIN));

    }


    @Test
    public void shouldContain_OWLEquivalentObjectPropertiesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(EQUIVALENT_OBJECT_PROPERTIES));

    }


    @Test
    public void shouldContain_OWLNegativeDataPropertyAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(NEGATIVE_DATA_PROPERTY_ASSERTION));

    }


    @Test
    public void shouldContain_OWLDifferentIndividualsAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DIFFERENT_INDIVIDUALS));

    }


    @Test
    public void shouldContain_OWLDisjointDataPropertiesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DISJOINT_DATA_PROPERTIES));

    }


    @Test
    public void shouldContain_OWLDisjointObjectPropertiesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DISJOINT_OBJECT_PROPERTIES));

    }


    @Test
    public void shouldContain_OWLObjectPropertyRangeAxiom() {
        assertThat(defaultAxiomTypes, hasItem(OBJECT_PROPERTY_RANGE));

    }


    @Test
    public void shouldContain_OWLObjectPropertyAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(OBJECT_PROPERTY_ASSERTION));

    }


    @Test
    public void shouldContain_OWLFunctionalObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(FUNCTIONAL_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLSubObjectPropertyOfAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SUB_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLDisjointUnionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DISJOINT_UNION));

    }


    @Test
    public void shouldContain_OWLSymmetricObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SYMMETRIC_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLDataPropertyRangeAxiom() {
        assertThat(defaultAxiomTypes, hasItem(OBJECT_PROPERTY_RANGE));

    }


    @Test
    public void shouldContain_OWLFunctionalDataPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(FUNCTIONAL_DATA_PROPERTY));

    }


    @Test
    public void shouldContain_OWLEquivalentDataPropertiesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(EQUIVALENT_DATA_PROPERTIES));

    }


    @Test
    public void shouldContain_OWLClassAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(CLASS_ASSERTION));

    }


    @Test
    public void shouldContain_OWLEquivalentClassesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(EQUIVALENT_CLASSES));

    }


    @Test
    public void shouldContain_OWLDataPropertyAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DATA_PROPERTY_ASSERTION));

    }


    @Test
    public void shouldContain_OWLTransitiveObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(TRANSITIVE_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLIrreflexiveObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(IRREFLEXIVE_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLSubDataPropertyOfAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SUB_DATA_PROPERTY));

    }


    @Test
    public void shouldContain_OWLInverseFunctionalObjectPropertyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(INVERSE_FUNCTIONAL_OBJECT_PROPERTY));

    }


    @Test
    public void shouldContain_OWLSameIndividualAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SAME_INDIVIDUAL));

    }


    @Test
    public void shouldContain_OWLSubPropertyChainOfAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SUB_PROPERTY_CHAIN_OF));

    }


    @Test
    public void shouldContain_OWLInverseObjectPropertiesAxiom() {
        assertThat(defaultAxiomTypes, hasItem(INVERSE_OBJECT_PROPERTIES));

    }


    @Test
    public void shouldContain_OWLHasKeyAxiom() {
        assertThat(defaultAxiomTypes, hasItem(HAS_KEY));

    }


    @Test
    public void shouldContain_OWLDatatypeDefinitionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(DATATYPE_DEFINITION));

    }


    @Test
    public void shouldContain_SWRLRule() {
        assertThat(defaultAxiomTypes, hasItem(SWRL_RULE));
    }


    @Test
    public void shouldContain_OWLAnnotationAssertionAxiom() {
        assertThat(defaultAxiomTypes, hasItem(ANNOTATION_ASSERTION));

    }


    @Test
    public void shouldContain_OWLSubAnnotationPropertyOfAxiom() {
        assertThat(defaultAxiomTypes, hasItem(SUB_ANNOTATION_PROPERTY_OF));

    }


    @Test
    public void shouldContain_OWLAnnotationPropertyDomainAxiom() {
        assertThat(defaultAxiomTypes, hasItem(ANNOTATION_PROPERTY_DOMAIN));

    }


    @Test
    public void shouldContain_OWLAnnotationPropertyRangeAxiom() {
        assertThat(defaultAxiomTypes, hasItem(ANNOTATION_PROPERTY_RANGE));

    }
}
