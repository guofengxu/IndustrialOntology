package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.kernel.frame.Mode;
import org.industrial.ontology.domain.core.DataFactory;
import org.semanticweb.owlapi.util.OWLEntityVisitorExAdapter;
import javax.annotation.Nonnull;
import java.util.Collections;

import java.util.HashSet;
import java.util.Set;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyClassValue;
import org.industrial.ontology.domain.frame.PlainPropertyDatatypeValue;
import org.industrial.ontology.domain.frame.PlainPropertyIndividualValue;
import org.industrial.ontology.domain.frame.PlainPropertyLiteralValue;
import org.industrial.ontology.domain.frame.PlainPropertyValueVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.PropertyValueTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-13
 */
class PropertyValueTranslator implements PlainPropertyValueVisitor<Set<OWLAxiom>> {

    private OWLEntity subject;

    private Mode mode;

    PropertyValueTranslator(OWLEntity subject,
                            Mode mode) {
        this.subject = subject;
        this.mode = mode;
    }

    @Override
    public Set<OWLAxiom> visit(final PlainPropertyClassValue propertyValue) {
        final OWLDataFactory df = DataFactory.get();
        final Set<OWLClassExpression> classExpressions = new HashSet<>();
        classExpressions.add(df.getOWLObjectSomeValuesFrom(propertyValue.getProperty(), propertyValue
                .getValue()
        ));
        if(mode == Mode.MAXIMAL) {
            classExpressions.add(df.getOWLObjectMinCardinality(1,
                                                               propertyValue.getProperty(),
                                                               propertyValue.getValue()));
        }
        return subject.accept(new OWLEntityVisitorExAdapter<>(null) {
            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLClass subject) {
                Set<OWLAxiom> result = new HashSet<>();
                for(OWLClassExpression ce : classExpressions) {
                    result.add(df.getOWLSubClassOfAxiom(subject, ce));
                }
                return result;
            }

            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLNamedIndividual subject) {
                Set<OWLAxiom> result = new HashSet<>();
                for(OWLClassExpression ce : classExpressions) {
                    result.add(df.getOWLClassAssertionAxiom(ce, subject));
                }
                return result;
            }
        });
    }

    @Override
    public Set<OWLAxiom> visit(final PlainPropertyIndividualValue propertyValue) {
        final OWLDataFactory df = DataFactory.get();
        final OWLClassExpression classExpression = df.getOWLObjectHasValue(propertyValue
                                                                                   .getProperty(),
                                                                           propertyValue
                                                                                   .getValue());
        return subject.accept(new OWLEntityVisitorExAdapter<>(null) {
            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLClass subject) {
                return Collections.singleton(df.getOWLSubClassOfAxiom(subject, classExpression));
            }

            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLNamedIndividual subject) {
                return Collections.singleton(df.getOWLObjectPropertyAssertionAxiom(propertyValue
                                                                                           .getProperty()
                        , subject, propertyValue
                                                                                           .getValue()
                ));
            }
        });
    }

    @Override
    public Set<OWLAxiom> visit(final PlainPropertyDatatypeValue propertyValue) {
        final OWLDataFactory df = DataFactory.get();
        final Set<OWLClassExpression> classExpressions = new HashSet<>();
        classExpressions.add(df.getOWLDataSomeValuesFrom(propertyValue.getProperty(), propertyValue
                .getValue()
        ));
        if(mode == Mode.MAXIMAL) {
            classExpressions.add(df.getOWLDataMinCardinality(1, propertyValue.getProperty(), propertyValue.getValue()));
        }
        return subject.accept(new OWLEntityVisitorExAdapter<>(null) {
            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLClass subject) {
                Set<OWLAxiom> result = new HashSet<>();
                for(OWLClassExpression ce : classExpressions) {
                    result.add(df.getOWLSubClassOfAxiom(subject, ce));
                }
                return result;
            }

            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLNamedIndividual subject) {
                Set<OWLAxiom> result = new HashSet<>();
                for(OWLClassExpression ce : classExpressions) {
                    result.add(df.getOWLClassAssertionAxiom(ce, subject));
                }
                return result;
            }
        });
    }

    @Override
    public Set<OWLAxiom> visit(final PlainPropertyLiteralValue propertyValue) {
        final OWLDataFactory df = DataFactory.get();
        final OWLClassExpression classExpression = df.getOWLDataHasValue(propertyValue
                                                                                 .getProperty()
                ,
                                                                         propertyValue.getValue());
        return subject.accept(new OWLEntityVisitorExAdapter<>(null) {
            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLClass subject) {
                return Collections.singleton(df.getOWLSubClassOfAxiom(subject, classExpression));
            }

            @Nonnull
            @Override
            public Set<OWLAxiom> visit(OWLNamedIndividual subject) {
                return Collections.singleton(df.getOWLDataPropertyAssertionAxiom(propertyValue
                                                                                         .getProperty()
                        , subject, propertyValue
                                                                                         .getValue()));
            }
        });
    }

    @Override
    public Set<OWLAxiom> visit(PlainPropertyAnnotationValue propertyValue) {
        OWLDataFactory df = DataFactory.get();
        return Collections.singleton(df.getOWLAnnotationAssertionAxiom(propertyValue
                                                                               .getProperty()
                , subject.getIRI(),
                                                                       propertyValue.getValue()));
    }
}
