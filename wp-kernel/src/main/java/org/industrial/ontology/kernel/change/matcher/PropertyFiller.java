package org.industrial.ontology.kernel.change.matcher;



import javax.annotation.Nonnull;

import java.util.Optional;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.matcher.PropertyFiller}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 13 Mar 2017
 */
public class PropertyFiller {

    private static final PropertyExtactor PROPERTY_EXTACTOR = new PropertyExtactor();

    private static final FillerExtractor FILLER_EXTRACTOR = new FillerExtractor();

    private final OWLClassExpression classExpression;

    private final OWLObject subject;

    public PropertyFiller(@Nonnull OWLObject subject,
                          @Nonnull OWLClassExpression classExpression) {
        this.classExpression = classExpression;
        this.subject = subject;
    }

    @Nonnull
    public OWLObject getSubject() {
        return subject;
    }

    @Nonnull
    public Optional<OWLProperty> getProperty() {
        return classExpression.accept(PROPERTY_EXTACTOR);
    }

    @Nonnull
    public Optional<OWLObject> getFiller() {
        return classExpression.accept(FILLER_EXTRACTOR);
    }

    public boolean isPropertyAndFillerPresent() {
        return getProperty().isPresent() && getFiller().isPresent();
    }

    private static class PropertyExtactor implements OWLClassExpressionVisitorEx<Optional<OWLProperty>> {

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLClass ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectIntersectionOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectUnionOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectComplementOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectSomeValuesFrom ce) {
            if (!ce.getProperty().isAnonymous()) {
                return Optional.of(ce.getProperty().asOWLObjectProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectAllValuesFrom ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectHasValue ce) {
            if (!ce.getProperty().isAnonymous()) {
                return Optional.of(ce.getProperty().asOWLObjectProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectMinCardinality ce) {
            if (!ce.getProperty().isAnonymous() && ce.getCardinality() == 1) {
                return Optional.of(ce.getProperty().asOWLObjectProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectExactCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectMaxCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectHasSelf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLObjectOneOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataSomeValuesFrom ce) {
            if (!ce.getProperty().isAnonymous()) {
                return Optional.of(ce.getProperty().asOWLDataProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataAllValuesFrom ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataHasValue ce) {
            if (!ce.getProperty().isAnonymous()) {
                return Optional.of(ce.getProperty().asOWLDataProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataMinCardinality ce) {
            if (!ce.getProperty().isAnonymous() && ce.getCardinality() == 1) {
                return Optional.of(ce.getProperty().asOWLDataProperty());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataExactCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLProperty> visit(@Nonnull OWLDataMaxCardinality ce) {
            return Optional.empty();
        }
    }

    private static class FillerExtractor implements OWLClassExpressionVisitorEx<Optional<OWLObject>> {

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLClass ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectIntersectionOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectUnionOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectComplementOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectSomeValuesFrom ce) {
            return Optional.of(ce.getFiller());
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectAllValuesFrom ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectHasValue ce) {
            return Optional.of(ce.getFiller());
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectMinCardinality ce) {
            if (ce.getCardinality() == 1) {
                return Optional.of(ce.getFiller());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectExactCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectMaxCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectHasSelf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLObjectOneOf ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataSomeValuesFrom ce) {
            return Optional.of(ce.getFiller());
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataAllValuesFrom ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataHasValue ce) {
            return Optional.of(ce.getFiller());
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataMinCardinality ce) {
            if (ce.getCardinality() == 1) {
                return Optional.of(ce.getFiller());
            }
            else {
                return Optional.empty();
            }
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataExactCardinality ce) {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Optional<OWLObject> visit(@Nonnull OWLDataMaxCardinality ce) {
            return Optional.empty();
        }
    }
}
