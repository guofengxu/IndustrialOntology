package org.industrial.ontology.domain.entity;



import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.core.PrimitiveType;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;

import java.util.Optional;
import java.util.function.Supplier;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.OWLPrimitiveData}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/12/2012
 * <p>
 *     A {@link OWLPrimitiveData} object wraps either an {@link OWLEntity}, an {@link OWLLiteral} or an
 *     {@link IRI}.
 * </p>
 */
public abstract class OWLPrimitiveData extends ObjectData implements Comparable<OWLPrimitiveData> {

    public Optional<IRI> asIRI() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public abstract OWLPrimitive getObject();

    public abstract ImmutableMap<DictionaryLanguage, String> getShortForms();

    /**
     * A convenience method that gets the first short form for this object
     */
    @Override
    public abstract String getBrowserText();

    protected String getFirstShortForm(Supplier<String> defaultValue) {
        return getShortForms()
                .values()
                .stream()
                .findFirst()
                .orElseGet(defaultValue);
    }

    public abstract <R, E extends Throwable> R accept(OWLPrimitiveDataVisitor<R, E> visitor) throws E;

    public abstract <R> R accept(OWLEntityVisitorEx<R> visitor, R defaultValue);

    public abstract PrimitiveType getType();

    public boolean isOWLEntity() {
        return getObject() instanceof OWLEntity;
    }

    public boolean isIRI() {
        return getObject() instanceof IRI;
    }

    public boolean isOWLLiteral() {
        return getObject() instanceof OWLLiteral;
    }

    public abstract Optional<OWLAnnotationValue> asAnnotationValue();

    public abstract Optional<OWLEntity> asEntity();

    public Optional<OWLLiteral> asLiteral() {
        return Optional.empty();
    }

    @Override
    public int compareTo(OWLPrimitiveData o) {
        return getBrowserText().compareToIgnoreCase(o.getBrowserText());
    }

    public boolean isDeprecated() {
        return false;
    }
}
