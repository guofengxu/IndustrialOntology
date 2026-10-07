package org.industrial.ontology.kernel.lucene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LuceneEntityDocument}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public record LuceneEntityDocument(@JsonProperty("entityType") @Nonnull EntityType<?> entityType, @JsonProperty("iri") @Nonnull IRI iri) {

    public LuceneEntityDocument {
        Objects.requireNonNull(entityType, "Null entityType");
        Objects.requireNonNull(iri, "Null iri");
    }

    @Nonnull
    @JsonCreator
    public static LuceneEntityDocument get(@Nonnull EntityType<?> entityType, @Nonnull IRI iri) {
        return new LuceneEntityDocument(entityType, iri);
    }

    @JsonProperty("entityType")
    @Nonnull
    public EntityType<?> getEntityType() {
        return entityType;
    }

    @JsonProperty("iri")
    @Nonnull
    public IRI getIri() {
        return iri;
    }
}
