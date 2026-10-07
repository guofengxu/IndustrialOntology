package org.industrial.ontology.domain.entity;



import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.core.PrimitiveType;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import javax.annotation.Nonnull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.OWLDataPropertyData}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/11/2012
 */
@AutoValue
public abstract class OWLDataPropertyData extends OWLPropertyData {

    public static OWLDataPropertyData get(@Nonnull OWLDataProperty property,
                                          @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms) {
        return new AutoValue_OWLDataPropertyData(shortForms, false, property);
    }

    public static OWLDataPropertyData get(@Nonnull OWLDataProperty property,
                                          @Nonnull ImmutableMap<DictionaryLanguage, String> shortForms,
                                          boolean deprecated) {
        return new AutoValue_OWLDataPropertyData(shortForms, deprecated, property);
    }

    @Nonnull
    @Override
    public abstract OWLDataProperty getObject();

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.DATA_PROPERTY;
    }

    @Override
    public boolean isOWLAnnotationProperty() {
        return false;
    }

    @Override
    public OWLDataProperty getEntity() {
        return getObject();
    }

    @Override
    public <R, E extends Throwable> R accept(OWLPrimitiveDataVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }


    @Override
    public <R> R accept(OWLEntityVisitorEx<R> visitor, R defaultValue) {
        return visitor.visit(getEntity());
    }

    @Override
    public <R> R accept(OWLEntityDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }
}
