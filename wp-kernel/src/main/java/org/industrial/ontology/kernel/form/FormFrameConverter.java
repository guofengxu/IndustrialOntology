package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.data.FormEntitySubject;
import org.industrial.ontology.domain.form.data.FormIriSubject;
import org.industrial.ontology.domain.form.data.FormSubject;
import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainClassFrame;

import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainObjectPropertyFrame;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormFrameConverter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-15
 */
public class FormFrameConverter {

    public FormFrameConverter() {
    }

    @Nonnull
    public Optional<PlainEntityFrame> toEntityFrame(@Nonnull FormFrame formFrame) {

        return formFrame.getSubject()
                 .accept(new FormSubject.FormDataSubjectVisitorEx<>() {
                     @Override
                     public Optional<PlainEntityFrame> visit(@Nonnull FormEntitySubject formDataEntitySubject) {
                         return getEntityFrame(formDataEntitySubject, formFrame);
                     }

                     @Override
                     public Optional<PlainEntityFrame> visit(@Nonnull FormIriSubject formDataIriSubject) {
                         return Optional.empty();
                     }
                 });
    }

    public Optional<PlainEntityFrame> getEntityFrame(@Nonnull FormEntitySubject formDataEntitySubject,
                                                                         @Nonnull FormFrame formFrame) {
        return formDataEntitySubject.getEntity()
                .accept(new OWLEntityVisitorEx<>() {
                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLClass cls) {
                        return Optional.of(PlainClassFrame.get(cls,
                                                          formFrame.getClasses(),
                                                          formFrame.getPropertyValues()));
                    }

                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLObjectProperty property) {
                        return Optional.of(PlainObjectPropertyFrame.get(property,
                                                                   getAnnotationPropertyValues(formFrame),
                                                                   ImmutableSet.of(),
                                                                   ImmutableSet.of(),
                                                                   ImmutableSet.of(),
                                                                   ImmutableSet.of()));
                    }

                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLDataProperty property) {
                        return Optional.of(PlainDataPropertyFrame.get(property,
                                                                 getAnnotationPropertyValues(formFrame),
                                                                 ImmutableSet.of(),
                                                                 ImmutableSet.of(),
                                                                 false));
                    }

                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLNamedIndividual individual) {
                        return Optional.of(PlainNamedIndividualFrame.get(individual,
                                                                    formFrame.getClasses(), ImmutableSet.<OWLNamedIndividual>of(),
                                                                         formFrame.getPropertyValues()));
                    }

                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLDatatype datatype) {
                        return Optional.empty();
                    }

                    @Nonnull
                    @Override
                    public Optional<PlainEntityFrame> visit(@Nonnull OWLAnnotationProperty property) {
                        return Optional.of(PlainAnnotationPropertyFrame.get(property,
                                                                       getAnnotationPropertyValues(formFrame),
                                                                       ImmutableSet.of(),
                                                                       ImmutableSet.of()));
                    }
                });
    }

    public ImmutableSet<PlainPropertyAnnotationValue> getAnnotationPropertyValues(@Nonnull FormFrame formFrame) {
        return formFrame.getPropertyValues()
        .stream()
        .filter(PlainPropertyValue::isAnnotation)
        .map(pv -> (PlainPropertyAnnotationValue) pv)
        .collect(toImmutableSet());
    }
}
