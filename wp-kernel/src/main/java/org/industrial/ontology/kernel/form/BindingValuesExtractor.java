package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.frame.ClassFrameProvider;
import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import javax.annotation.Nonnull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;

import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;

import org.industrial.ontology.kernel.api.index.ObjectPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.OwlClassBinding;
import org.industrial.ontology.domain.form.field.OwlInstanceBinding;
import org.industrial.ontology.domain.form.field.OwlPropertyBinding;
import org.industrial.ontology.domain.form.field.OwlSubClassBinding;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.BindingValuesExtractor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-24
 */
public class BindingValuesExtractor {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final ClassAssertionAxiomsByIndividualIndex classAssertionAxiomsByIndividualIndex;

    @Nonnull
    private final ClassHierarchyProvider classHierarchyProvider;

    @Nonnull
    private final ObjectPropertyAssertionAxiomsBySubjectIndex objectPropertyAssertionAxiomsBySubjectIndex;

    @Nonnull
    private final DataPropertyAssertionAxiomsBySubjectIndex dataPropertyAssertionAxiomsBySubjectIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubjectIndex;

    @Nonnull
    private final ClassAssertionAxiomsByClassIndex classAssertionAxiomsByClassIndex;

    @Nonnull
    private final ClassFrameProvider classFrameProvider;

    public BindingValuesExtractor(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                  @Nonnull ClassAssertionAxiomsByIndividualIndex classAssertionAxiomsByIndividualIndex,
                                  @Nonnull ClassHierarchyProvider classHierarchyProvider,
                                  @Nonnull ObjectPropertyAssertionAxiomsBySubjectIndex objectPropertyAssertionAxiomsBySubjectIndex,
                                  @Nonnull DataPropertyAssertionAxiomsBySubjectIndex dataPropertyAssertionAxiomsBySubjectIndex,
                                  @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubjectIndex,
                                  @Nonnull ClassAssertionAxiomsByClassIndex classAssertionAxiomsByClassIndex,
                                  @Nonnull ClassFrameProvider classFrameProvider) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.classAssertionAxiomsByIndividualIndex = classAssertionAxiomsByIndividualIndex;
        this.classHierarchyProvider = classHierarchyProvider;
        this.objectPropertyAssertionAxiomsBySubjectIndex = objectPropertyAssertionAxiomsBySubjectIndex;
        this.dataPropertyAssertionAxiomsBySubjectIndex = dataPropertyAssertionAxiomsBySubjectIndex;
        this.annotationAssertionAxiomsBySubjectIndex = annotationAssertionAxiomsBySubjectIndex;
        this.classAssertionAxiomsByClassIndex = classAssertionAxiomsByClassIndex;
        this.classFrameProvider = classFrameProvider;
    }

    @Nonnull
    public ImmutableList<OWLPrimitive> getBindingValues(@Nonnull OWLEntity formSubject,
                                                        @Nonnull OwlBinding binding) {
        OWLEntityVisitorEx<ImmutableList<OWLPrimitive>> subjectVisitor = new OWLEntityVisitorEx<>() {
            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLClass cls) {
                return getBindingsForClass(cls, binding);
            }

            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLObjectProperty property) {
                return ImmutableList.of();
            }

            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLDataProperty property) {
                return ImmutableList.of();
            }

            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLNamedIndividual individual) {
                return getBindingsForIndividual(individual, binding);
            }

            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLDatatype datatype) {
                return ImmutableList.of();
            }

            @Nonnull
            @Override
            public ImmutableList<OWLPrimitive> visit(@Nonnull OWLAnnotationProperty property) {
                return ImmutableList.of();
            }
        };
        return formSubject.accept(subjectVisitor);
    }

    @Nonnull
    private ImmutableList<OWLPrimitive> getBindingsForClass(@Nonnull OWLClass subject,
                                                            @Nonnull OwlBinding binding) {
        if(binding instanceof OwlPropertyBinding) {
            var property = ((OwlPropertyBinding) binding).getProperty();
            if(property.isOWLAnnotationProperty()) {
                return projectOntologiesIndex.getOntologyIds()
                                             .flatMap(ontId -> annotationAssertionAxiomsBySubjectIndex.getAxiomsForSubject(
                                                     subject.getIRI(),
                                                     ontId))
                                             .filter(ax -> ax.getProperty().equals(property))
                                             .map(OWLAnnotationAssertionAxiom::getValue)
                                             .sorted()
                                             .collect(toImmutableList());
            }
            // Fallback to frame for non-annotation assertions, for now
            return classFrameProvider.getFrame(subject, ClassFrameTranslationOptions.defaultOptions())
                              .getPropertyValues()
                              .stream()
                              .filter(pv -> pv.getProperty().equals(property))
                              .map(PlainPropertyValue::getValue)
                                     .sorted()
                              .collect(toImmutableList());
        }
        else if(binding instanceof OwlClassBinding) {
            return classHierarchyProvider.getParents(subject).stream().sorted().collect(toImmutableList());

        }
        else if(binding instanceof OwlInstanceBinding) {
            return projectOntologiesIndex.getOntologyIds()
                                         .flatMap(ontId -> classAssertionAxiomsByClassIndex.getClassAssertionAxioms(
                                                 subject,
                                                 ontId))
                                         .map(OWLClassAssertionAxiom::getIndividual)
                                         .filter(OWLIndividual::isNamed)
                                         .map(OWLIndividual::asOWLNamedIndividual)
                                         .sorted()
                                         .collect(toImmutableList());

        }
        else if(binding instanceof OwlSubClassBinding) {
            return classHierarchyProvider.getChildren(subject).stream().sorted().collect(toImmutableList());
        }
        else {
            return ImmutableList.of();
        }
    }

    @Nonnull
    private ImmutableList<OWLPrimitive> getBindingsForIndividual(@Nonnull OWLNamedIndividual individual,
                                                                 @Nonnull OwlBinding binding) {
        if(binding instanceof OwlPropertyBinding) {
            var property = ((OwlPropertyBinding) binding).getProperty();
            if(property.isOWLAnnotationProperty()) {
                return projectOntologiesIndex.getOntologyIds()
                                             .flatMap(ontId -> annotationAssertionAxiomsBySubjectIndex.getAxiomsForSubject(
                                                     individual.getIRI(),
                                                     ontId))
                                             .filter(ax -> ax.getProperty().equals(property))
                                             .map(OWLAnnotationAssertionAxiom::getValue)
                                             .sorted()
                                             .collect(toImmutableList());
            }
            else if(property.isOWLDataProperty()) {
                return projectOntologiesIndex.getOntologyIds()
                                             .flatMap(ontId -> dataPropertyAssertionAxiomsBySubjectIndex.getDataPropertyAssertions(
                                                     individual,
                                                     ontId))
                                             .filter(ax -> ax.getProperty().equals(property))
                                             .map(OWLDataPropertyAssertionAxiom::getObject)
                                             .sorted()
                                             .collect(toImmutableList());
            }
            else if(property.isOWLObjectProperty()) {
                return projectOntologiesIndex.getOntologyIds()
                                             .flatMap(ontId -> objectPropertyAssertionAxiomsBySubjectIndex.getObjectPropertyAssertions(
                                                     individual,
                                                     ontId))
                                             .filter(ax -> ax.getProperty().equals(property))
                                             .map(OWLObjectPropertyAssertionAxiom::getObject)
                                             .filter(OWLIndividual::isNamed)
                                             .map(OWLIndividual::asOWLNamedIndividual)
                                             .sorted()
                                             .collect(toImmutableList());
            }
            else {
                // Shouldn't happen
                return ImmutableList.of();
            }
        }
        else if(binding instanceof OwlClassBinding) {
            return projectOntologiesIndex.getOntologyIds()
                                         .flatMap(ontId ->
                                                          classAssertionAxiomsByIndividualIndex.getClassAssertionAxioms(
                                                                  individual,
                                                                  ontId))
                                         .map(OWLClassAssertionAxiom::getClassExpression)
                                         .filter(OWLClassExpression::isNamed)
                                         .distinct()
                                         .map(OWLClassExpression::asOWLClass)
                                         .sorted()
                                         .collect(toImmutableList());

        }
        else {
            return ImmutableList.of();
        }
    }
}
