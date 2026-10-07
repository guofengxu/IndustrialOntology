package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.match.Matcher;
import org.industrial.ontology.kernel.match.MatcherFactory;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.OwlClassBinding;
import org.industrial.ontology.domain.form.field.OwlInstanceBinding;
import org.industrial.ontology.domain.form.field.OwlPropertyBinding;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLPrimitive;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityFrameMapper}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-06
 */
public class EntityFrameMapper {

    @Nonnull
    private final PlainEntityFrame frame;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final ClassAssertionAxiomsByClassIndex index;

    @Nonnull
    private final RenderingManager renderingManager;

    @Nonnull
    private final MatcherFactory matcherFactory;


    public EntityFrameMapper(@Nonnull PlainEntityFrame frame,
                             @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                             @Nonnull ClassAssertionAxiomsByClassIndex index,
                             @Nonnull RenderingManager renderingManager,
                             @Nonnull MatcherFactory matcherFactory) {
        this.frame = checkNotNull(frame);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.index = checkNotNull(index);
        this.renderingManager = checkNotNull(renderingManager);
        this.matcherFactory = checkNotNull(matcherFactory);
    }

    @Nonnull
    public ImmutableList<OWLPrimitive> getValues(@Nonnull OwlBinding binding) {
        if(binding instanceof OwlClassBinding) {
            if(frame instanceof PlainClassFrame) {
                return ImmutableList.copyOf(((PlainClassFrame) frame).getParents());
            }
            else if(frame instanceof PlainNamedIndividualFrame) {
                return ImmutableList.copyOf(((PlainNamedIndividualFrame) frame).getParents());
            }
            else {
                return ImmutableList.of();
            }
        }
        else if(binding instanceof OwlInstanceBinding) {
            if(frame instanceof PlainClassFrame) {
                var classFrame = (PlainClassFrame) frame;
                var subjectCls = classFrame.getSubject();
                return projectOntologiesIndex.getOntologyIds()
                                             .flatMap(ontId -> index.getClassAssertionAxioms(subjectCls, ontId))
                                             .map(OWLClassAssertionAxiom::getIndividual)
                                             .filter(OWLIndividual::isNamed)
                                             .map(OWLIndividual::asOWLNamedIndividual)
                                             .collect(toImmutableList());
            }
            else {
                return ImmutableList.of();
            }
        }
        else {
            var propertyBinding = (OwlPropertyBinding) binding;
            var matcher = propertyBinding.getValuesCriteria()
                                         .map(matcherFactory::getRelationshipValueMatcher)
                                         .orElse(p -> true);
            return frame.getPropertyValues()
                        .stream()
                        .filter(propertyValue -> propertyMatches(propertyValue, propertyBinding))
                        .filter(propertyValue -> valueMatches(matcher, propertyValue))
                        .map(PlainPropertyValue::getValue)
                        .collect(toImmutableList());
        }
    }

    public boolean propertyMatches(PlainPropertyValue propertyValue, OwlPropertyBinding propertyBinding) {
        return propertyValue.getProperty()
                            .equals(propertyBinding.getProperty());
    }

    public boolean valueMatches(Matcher<OWLPrimitive> matcher, PlainPropertyValue propertyValue) {
        return matcher.matches(propertyValue.getValue());
    }
}
