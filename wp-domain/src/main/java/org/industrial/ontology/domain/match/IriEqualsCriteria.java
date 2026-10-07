package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.IriEqualsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("IriEquals")
public record IriEqualsCriteria(@Nonnull @JsonProperty(IriEqualsCriteria.IRI_PROPERTY) IRI iri) implements IriCriteria, AnnotationPropertyCriteria {

    public IriEqualsCriteria {
        Objects.requireNonNull(iri, "Null iri");
    }

    private static final String IRI_PROPERTY = "iri";

    @Nonnull
    @JsonCreator
    public static IriEqualsCriteria get(@Nonnull @JsonProperty(IRI_PROPERTY) IRI iri) {
        return new IriEqualsCriteria(iri);
    }

    @Nonnull
    public static IriEqualsCriteria iriEqualTo(@Nonnull String iri) {
        return get(IRI.create(iri));
    }

    @Nonnull
    public static IriEqualsCriteria isRdfsLabel() {
        return get(OWLRDFVocabulary.RDFS_LABEL.getIRI());
    }

    @Nonnull
    public static IriEqualsCriteria isOwlDeprecated() {
        return get(OWLRDFVocabulary.OWL_DEPRECATED.getIRI());
    }

    @Nonnull
    public static IriEqualsCriteria get(@Nonnull OWLAnnotationProperty property) {
        return get(property.getIRI());
    }

    @Override
    public <R> R accept(@Nonnull AnnotationPropertyCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @JsonProperty(IRI_PROPERTY)
    public IRI getIri() {
        return iri;
    }
}
