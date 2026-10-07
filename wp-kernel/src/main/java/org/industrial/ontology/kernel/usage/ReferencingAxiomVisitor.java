package org.industrial.ontology.kernel.usage;



import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.domain.usage.UsageReference;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

import java.util.Optional;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.usage.ReferencingAxiomVisitor}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 11/07/2013
 */
public class ReferencingAxiomVisitor implements OWLAxiomVisitorEx<Set<UsageReference>> {

    @Nonnull
    private final OWLEntity usageOf;

    @Nonnull
    private final RenderingManager renderingManager;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesInSignatureIndex;

    public ReferencingAxiomVisitor(@Nonnull OWLEntity usageOf,
                                   @Nonnull RenderingManager renderingManager,
                                   @Nonnull EntitiesInProjectSignatureByIriIndex entitiesInSignatureIndex) {
        this.usageOf = checkNotNull(usageOf);
        this.renderingManager = checkNotNull(renderingManager);
        this.entitiesInSignatureIndex = checkNotNull(entitiesInSignatureIndex);
    }

    private Set<UsageReference> translate(Collection<? extends OWLObject> subjects, OWLAxiom axiom) {
        return subjects.stream()
                .flatMap(subject -> translate(subject, axiom).stream())
                .collect(toSet());
    }

    private Set<UsageReference> translate(OWLObject subject, OWLAxiom axiom) {
        Optional<OWLEntity> axiomSubject = Optional.empty();
        if(subject instanceof OWLEntity) {
            axiomSubject = Optional.of((OWLEntity) subject);
        }
        else if(subject instanceof IRI) {
            if(subject.equals(usageOf.getIRI())) {
                return translate(usageOf, axiom);
            }
            else {
                var entities = entitiesInSignatureIndex
                        .getEntitiesInSignature((IRI) subject)
                        .collect(toList());
                return translate(entities, axiom);
            }

        }
        else if(subject instanceof SWRLAtom) {
            var predicate = ((SWRLAtom) subject).getPredicate();
            if (predicate instanceof OWLEntity) {
                return translate((OWLEntity) predicate, axiom);
            }
        }
        var useageOfBrowserText = renderingManager.getShortForm(usageOf);
        var rendering = renderingManager.getHTMLBrowserText(axiom, Collections.singleton(useageOfBrowserText));
        var subjectRendering = axiomSubject.map(renderingManager::getShortForm);
        return Collections.singleton(new UsageReference(axiom.getAxiomType(), rendering, axiomSubject, subjectRendering));
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSubClassOfAxiom axiom) {
        return translate(axiom.getSubClass(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return translate(axiom.getSubject(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDisjointClassesAxiom axiom) {
        return translate((OWLEntity) null, axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
        return translate(axiom.getProperties(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
        return translate(axiom.getSubject(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
        return translate((OWLEntity) null, axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
        return translate(axiom.getProperties(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
        return translate(axiom.getProperties(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
        return translate(axiom.getSubject(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
        return translate(axiom.getSubProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDisjointUnionAxiom axiom) {
        return translate(axiom.getOWLClass(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDeclarationAxiom axiom) {
        return translate(axiom.getEntity(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLAnnotationAssertionAxiom axiom) {
        return translate(axiom.getSubject(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
        return translate(axiom.getProperties(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLClassAssertionAxiom axiom) {
        return translate(axiom.getIndividual(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
        if(axiom.containsNamedEquivalentClass()) {
            return translate(axiom.getNamedClasses(), axiom);
        }
        else {
            return translate(axiom.getClassExpressions().iterator().next(), axiom);
        }

    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
        return translate(axiom.getSubject(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
        return translate(axiom.getSubProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSameIndividualAxiom axiom) {
        return translate(axiom.getIndividuals(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
        return translate(axiom.getSuperProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
        return translate(axiom.getProperties(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLHasKeyAxiom axiom) {
        return translate(axiom.getClassExpression(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
        return translate(axiom.getDatatype(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull SWRLRule rule) {
        return translate(rule.getHead(), rule);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLSubAnnotationPropertyOfAxiom axiom) {
        return translate(axiom.getSubProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLAnnotationPropertyDomainAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }

    @Nonnull
    @Override
    public Set<UsageReference> visit(@Nonnull OWLAnnotationPropertyRangeAxiom axiom) {
        return translate(axiom.getProperty(), axiom);
    }
}
