package org.industrial.ontology.domain.entity;



import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.core.PrimitiveType;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import javax.annotation.Nonnull;

import java.util.Optional;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.IRIData}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 06/12/2012
 */
@AutoValue
public abstract class IRIData extends OWLPrimitiveData {

    public static IRIData get(@Nonnull IRI iri,
                              @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms) {
        return new AutoValue_IRIData(shortForms, iri);
    }

    @Nonnull
    @Override
    public abstract IRI getObject();

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.IRI;
    }

    @Override
    public <R, E extends Throwable> R accept(OWLPrimitiveDataVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(OWLEntityVisitorEx<R> visitor, R defaultValue) {
        return defaultValue;
    }

    @Override
    public String getBrowserText() {
        return getObject().toString();
    }

    public boolean isHTTPLink() {
        return "http".equalsIgnoreCase(getObject().getScheme())
                || "https".equalsIgnoreCase(getObject().getScheme());
    }

    public boolean isWikipediaLink() {
        return isHTTPLink() && getObject().toString().contains("wikipedia.org/wiki/");
    }

    @Override
    public String getUnquotedBrowserText() {
        return getObject().toString();
    }

    @Override
    public Optional<OWLAnnotationValue> asAnnotationValue() {
        return Optional.of(getObject());
    }

    @Override
    public Optional<OWLEntity> asEntity() {
        return Optional.empty();
    }

    @Override
    public Optional<IRI> asIRI() {
        return Optional.of(getObject());
    }
}
