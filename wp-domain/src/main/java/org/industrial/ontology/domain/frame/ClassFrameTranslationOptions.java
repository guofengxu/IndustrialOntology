package org.industrial.ontology.domain.frame;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ClassFrameTranslationOptions}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public record ClassFrameTranslationOptions(AncestorsTreatment ancestorsTreatment, @Nonnull RelationshipTranslationOptions relationshipTranslationOptions) {

    public ClassFrameTranslationOptions {
        Objects.requireNonNull(ancestorsTreatment, "Null ancestorsTreatment");
        Objects.requireNonNull(relationshipTranslationOptions, "Null relationshipTranslationOptions");
    }

    public static ClassFrameTranslationOptions defaultOptions() {
        return get(AncestorsTreatment.EXCLUDE_ANCESTORS, RelationshipTranslationOptions.get(RelationshipTranslationOptions.allOutgoingRelationships(), RelationshipTranslationOptions.noIncomingRelationships(), RelationshipTranslationOptions.RelationshipMinification.NON_MINIMIZED_RELATIONSHIPS));
    }

    public enum AncestorsTreatment {

        INCLUDE_ANCESTORS, EXCLUDE_ANCESTORS
    }

    public static ClassFrameTranslationOptions get(@Nonnull AncestorsTreatment ancestorsTreatment, @Nonnull RelationshipTranslationOptions relationshipTranslationOptions) {
        return new ClassFrameTranslationOptions(ancestorsTreatment, relationshipTranslationOptions);
    }

    public AncestorsTreatment getAncestorsTreatment() {
        return ancestorsTreatment;
    }

    @Nonnull
    public RelationshipTranslationOptions getRelationshipTranslationOptions() {
        return relationshipTranslationOptions;
    }
}
