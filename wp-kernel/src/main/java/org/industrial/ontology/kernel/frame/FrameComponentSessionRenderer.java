package org.industrial.ontology.kernel.frame;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.frame.FrameComponentRenderer;
import javax.annotation.Nonnull;
import java.util.HashMap;

import java.util.Map;
import java.util.function.Supplier;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;

import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLLiteralData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.FrameComponentSessionRenderer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-01
 */
public class FrameComponentSessionRenderer implements FrameComponentRenderer {

    private final Map<Object, Object> cache = new HashMap<>();

    @Nonnull
    private final FrameComponentRenderer delegate;

    public FrameComponentSessionRenderer(@Nonnull FrameComponentRenderer delegate) {
        this.delegate = checkNotNull(delegate);
    }


    @SuppressWarnings("unchecked")
    private <K, V> V get(K key, Supplier<V> loader) {
        var v = cache.get(key);
        if(v == null) {
            v = loader.get();
            cache.put(key, v);
        }
        return (V) v;
    }

    @Nonnull
    @Override
    public OWLClassData getRendering(@Nonnull OWLClass cls) {
        return get(cls, () -> delegate.getRendering(cls));
    }

    @Nonnull
    @Override
    public OWLObjectPropertyData getRendering(@Nonnull OWLObjectProperty property) {
        return get(property, () -> delegate.getRendering(property));
    }

    @Nonnull
    @Override
    public OWLDataPropertyData getRendering(@Nonnull OWLDataProperty property) {
        return get(property, () -> delegate.getRendering(property));
    }

    @Nonnull
    @Override
    public OWLAnnotationPropertyData getRendering(@Nonnull OWLAnnotationProperty property) {
        return get(property, () -> delegate.getRendering(property));
    }

    @Nonnull
    @Override
    public OWLNamedIndividualData getRendering(@Nonnull OWLNamedIndividual individual) {
        return get(individual, () -> delegate.getRendering(individual));
    }

    @Nonnull
    @Override
    public OWLDatatypeData getRendering(@Nonnull OWLDatatype datatype) {
        return get(datatype, () -> delegate.getRendering(datatype));
    }

    @Nonnull
    @Override
    public OWLLiteralData getRendering(@Nonnull OWLLiteral literal) {
        return get(literal, () -> delegate.getRendering(literal));
    }

    @Nonnull
    @Override
    public OWLPrimitiveData getRendering(@Nonnull OWLAnnotationValue annotationValue) {
        return get(annotationValue, () -> delegate.getRendering(annotationValue));
    }

    @Nonnull
    @Override
    public ImmutableSet<OWLEntityData> getRendering(@Nonnull IRI iri) {
        return get(iri, () -> delegate.getRendering(iri));
    }

    @Nonnull
    @Override
    public OWLEntityData getEntityRendering(@Nonnull OWLEntity entity) {
        return get(entity, () -> delegate.getEntityRendering(entity));
    }
}
