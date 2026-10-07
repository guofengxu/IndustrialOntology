package org.industrial.ontology.kernel.crud;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.match.Matcher;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.domain.crud.ConditionalIriPrefix;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.match.CompositeHierarchyPositionCriteria;
import org.industrial.ontology.domain.match.RootCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityIriPrefixResolver_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-08
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityIriPrefixResolverTest {

    public static final String FALLBACK_IRI_PREFIX = "FallbackIriPrefix";

    private static final String CONDITIONAL_IRI_PREFIX = "ConditionalIriPrefix";

    private EntityIriPrefixResolver resolver;

    @Mock
    private MatcherFactory matcherFactory;

    @Mock
    private RootCriteria criteria;

    @Mock
    private Matcher<OWLEntity> matcher;

    @Mock
    private OWLEntity parentEntity;

    @Mock
    private OWLEntity otherParent;

    @Mock
    private EntityCrudKitPrefixSettings prefixSettings;

    @Mock
    private ConditionalIriPrefix conditionalPrefix;

    @Mock
    private CompositeHierarchyPositionCriteria hierarchyCriteria;

    @Mock
    private EntityIriPrefixCriteriaRewriter entityIriPrefixCriteriaRewriter;

    @BeforeEach
    public void setUp() {
        resolver = new EntityIriPrefixResolver(matcherFactory, entityIriPrefixCriteriaRewriter);
        when(entityIriPrefixCriteriaRewriter.rewriteCriteria(hierarchyCriteria)).thenReturn(criteria);
        when(prefixSettings.getConditionalIriPrefixes()).thenReturn(ImmutableList.of(conditionalPrefix));
        when(prefixSettings.getIRIPrefix()).thenReturn(FALLBACK_IRI_PREFIX);
        when(conditionalPrefix.getCriteria()).thenReturn(hierarchyCriteria);
        when(conditionalPrefix.getIriPrefix()).thenReturn(CONDITIONAL_IRI_PREFIX);
        when(matcherFactory.getMatcher(criteria)).thenReturn(matcher);
    }

    @Test
    public void shouldReturnFallbackPrefixForNonMatch() {
        var resolvedPrefix = resolver.getIriPrefix(prefixSettings, ImmutableList.of(otherParent));
        assertThat(resolvedPrefix, is(FALLBACK_IRI_PREFIX));
    }

    @Test
    public void shouldReturnFallbackPrefixForNonMatchOfEmptyParents() {
        var resolvedPrefix = resolver.getIriPrefix(prefixSettings, ImmutableList.of());
        assertThat(resolvedPrefix, is(FALLBACK_IRI_PREFIX));
    }

    @Test
    public void shouldReturnMatchedPrefixForMatch() {
        when(matcher.matches(parentEntity)).thenReturn(true);
        var resolvedPrefix = resolver.getIriPrefix(prefixSettings, ImmutableList.of(parentEntity));
        assertThat(resolvedPrefix, is(CONDITIONAL_IRI_PREFIX));
    }
}
