package org.industrial.ontology.kernel.change.description;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.MergedEntities}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record MergedEntities(ImmutableSet<IRI> mergedEntities, IRI targetEntity) implements StructuredChangeDescription {

    public MergedEntities {
        Objects.requireNonNull(mergedEntities, "Null mergedEntities");
        Objects.requireNonNull(targetEntity, "Null targetEntity");
    }

    private static final String MERGED_ENTITIES = "MergedEntities";

    public static MergedEntities get(@Nonnull ImmutableSet<IRI> mergedEntities, @Nonnull IRI targetEntity) {
        return new MergedEntities(mergedEntities, targetEntity);
    }

    @Nonnull
    public static String getAssociatedTypeName() {
        return MERGED_ENTITIES;
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return MERGED_ENTITIES;
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Merged %s into %s", getMergedEntities(), getTargetEntity());
    }

    public ImmutableSet<IRI> getMergedEntities() {
        return mergedEntities;
    }

    public IRI getTargetEntity() {
        return targetEntity;
    }
}
