package org.industrial.ontology.kernel.axiom;



import org.industrial.ontology.domain.object.OWLObjectSelector;
import javax.annotation.Nonnull;

import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;

import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.axiom.AxiomSubjectProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 31/01/15
 */
public class AxiomSubjectProvider {

    private final SubjectProvider subjectProvider;

    /**
     * Constructs an AxiomSubjectProvider.
     * @param classExpressionSelector A selector for class expressions that chooses a class expression to be the
     *                                subject.  This is used for nary class axioms, for example
     *                                {@link OWLEquivalentClassesAxiom} axioms.  Not {@code null}.
     * @param objectPropertyExpressionSelector A selector for object properties that chooses an object property to be
     *                                         the subject.  This is used for nary property axioms, for example,
     *                                         {@link OWLEquivalentObjectPropertiesAxiom}.  Not {@code null}.
     * @param dataPropertyExpressionSelector A selector for data properties that chooses a data property to be the
     *                                       subject.  This is used for nary property axioms, for example
     *                                       {@link OWLEquivalentDataPropertiesAxiom} axioms.  Not {@code null}.
     * @param individualSelector A selector for individuals that chooses an individual to be the subject.  This is used
     *                           for nary individual axioms, for example {@link OWLSameIndividualAxiom} axioms.
     * @param atomSelector A selector for SWRL atoms that chooses an atom to be the subject.  This is used to select
     *                     an atom that is in the head of a {@link SWRLRule} to be the subject. Not {@code null}.
     * @throws java.lang.NullPointerException if any parameters are {@code null}.
     */
    public AxiomSubjectProvider(OWLObjectSelector<OWLClassExpression> classExpressionSelector,
                                OWLObjectSelector<OWLObjectPropertyExpression> objectPropertyExpressionSelector,
                                OWLObjectSelector<OWLDataPropertyExpression> dataPropertyExpressionSelector,
                                OWLObjectSelector<OWLIndividual> individualSelector,
                                OWLObjectSelector<SWRLAtom> atomSelector) {
        subjectProvider = new SubjectProvider(
                classExpressionSelector,
                objectPropertyExpressionSelector,
                dataPropertyExpressionSelector,
                individualSelector,
                atomSelector);
    }

    /**
     * Gets the subject of the specified axiom.
     * @param axiom The axiom.  Not {@code null}.
     * @return The (possibly absent) subject.  Not {@code null}.
     */
    public Optional<? extends OWLObject> getSubject(OWLAxiom axiom) {
        return checkNotNull(axiom).accept(subjectProvider);
    }




    private static class SubjectProvider implements OWLAxiomVisitorEx<Optional<? extends OWLObject>> {

        private final OWLObjectSelector<OWLClassExpression> classExpressionSelector;

        private final OWLObjectSelector<OWLObjectPropertyExpression> objectPropertyExpressionSelector;

        private final OWLObjectSelector<OWLDataPropertyExpression> dataPropertyExpressionSelector;

        private final OWLObjectSelector<OWLIndividual> individualSelector;

        private final OWLObjectSelector<SWRLAtom> atomSelector;


        public SubjectProvider(OWLObjectSelector<OWLClassExpression> classExpressionSelector,
                               OWLObjectSelector<OWLObjectPropertyExpression> objectPropertyExpressionSelector,
                               OWLObjectSelector<OWLDataPropertyExpression> dataPropertyExpressionSelector,
                               OWLObjectSelector<OWLIndividual> individualSelector,
                               OWLObjectSelector<SWRLAtom> atomSelector) {
            this.classExpressionSelector = checkNotNull(classExpressionSelector);
            this.objectPropertyExpressionSelector = checkNotNull(objectPropertyExpressionSelector);
            this.individualSelector = checkNotNull(individualSelector);
            this.dataPropertyExpressionSelector = checkNotNull(dataPropertyExpressionSelector);
            this.atomSelector = checkNotNull(atomSelector);
        }

        public static Optional<? extends OWLObject> wrap(OWLObject object) {
            return Optional.of(object);
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSubClassOfAxiom axiom) {
            return wrap(axiom.getSubClass());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
            return wrap(axiom.getSubject());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDisjointClassesAxiom axiom) {
            return classExpressionSelector.selectOne(axiom.getClassExpressions());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
            return objectPropertyExpressionSelector.selectOne(axiom.getProperties());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
            return wrap(axiom.getSubject());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
            return individualSelector.selectOne(axiom.getIndividuals());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
            return dataPropertyExpressionSelector.selectOne(axiom.getProperties());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
            return objectPropertyExpressionSelector.selectOne(axiom.getProperties());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
            return wrap(axiom.getSubject());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
            return wrap(axiom.getSubProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDisjointUnionAxiom axiom) {
            return wrap(axiom.getOWLClass());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDeclarationAxiom axiom) {
            return wrap(axiom.getEntity());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLAnnotationAssertionAxiom axiom) {
            return wrap(axiom.getSubject());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
            return dataPropertyExpressionSelector.selectOne(axiom.getProperties());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLClassAssertionAxiom axiom) {
            return wrap(axiom.getIndividual());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
            return classExpressionSelector.selectOne(axiom.getClassExpressions());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
            return wrap(axiom.getSubject());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
            return wrap(axiom.getSubProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSameIndividualAxiom axiom) {
            return individualSelector.selectOne(axiom.getIndividuals());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
            return wrap(axiom.getSuperProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
            return wrap(axiom.getFirstProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLHasKeyAxiom axiom) {
            return wrap(axiom.getClassExpression());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
            return wrap(axiom.getDatatype());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull SWRLRule axiom) {
            return atomSelector.selectOne(axiom.getHead());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLSubAnnotationPropertyOfAxiom axiom) {
            return wrap(axiom.getSubProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLAnnotationPropertyDomainAxiom axiom) {
            return wrap(axiom.getProperty());
        }

        @Nonnull
        @Override
        public Optional<? extends OWLObject> visit(@Nonnull OWLAnnotationPropertyRangeAxiom axiom) {
            return wrap(axiom.getProperty());
        }
    }
}
