package org.industrial.ontology.kernel.crud;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.domain.crud.ConditionalIriPrefix;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityIriPrefixResolver}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-08
 */
public class EntityIriPrefixResolver {

    @Nonnull
    private final MatcherFactory matcherFactory;

    @Nonnull
    private final EntityIriPrefixCriteriaRewriter entityIriPrefixCriteriaRewriter;

    public EntityIriPrefixResolver(@Nonnull MatcherFactory matcherFactory,
                                   @Nonnull EntityIriPrefixCriteriaRewriter entityIriPrefixCriteriaRewriter) {
        this.matcherFactory = matcherFactory;
        this.entityIriPrefixCriteriaRewriter = entityIriPrefixCriteriaRewriter;
    }

    /**
     * Gets the IRI prefix for the specified prefix settings and the specified
     * parents.
     * @param prefixSettings The prefix settings.
     * @param parentEntities The parent entities.  May be empty.  If the parents is empty
     *                       then the fallback prefix will be returned.
     * @return The resolved prefix.
     */
    @Nonnull
    public String getIriPrefix(@Nonnull EntityCrudKitPrefixSettings prefixSettings,
                               @Nonnull ImmutableList<OWLEntity> parentEntities) {
        if(parentEntities.isEmpty()) {
            return prefixSettings.getIRIPrefix();
        }
        var conditionalPrefixes = prefixSettings.getConditionalIriPrefixes();
        for(var conditionalPrefix : conditionalPrefixes) {
            if(allEntitiesMatch(parentEntities, conditionalPrefix)) {
                return conditionalPrefix.getIriPrefix();
            }
        }
        return prefixSettings.getIRIPrefix();
    }

    private boolean allEntitiesMatch(@Nonnull ImmutableList<OWLEntity> parentEntities,
                                     @Nonnull ConditionalIriPrefix conditionalPrefix) {
        var criteria = conditionalPrefix.getCriteria();
        var rewrittenCriteria = entityIriPrefixCriteriaRewriter.rewriteCriteria(criteria);
        var matcher = matcherFactory.getMatcher(rewrittenCriteria);
        for(var parent : parentEntities) {
            if(!matcher.matches(parent)) {
                return false;
            }
        }
        return true;
    }
}
