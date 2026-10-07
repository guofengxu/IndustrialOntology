package org.industrial.ontology.kernel.mansyntax.render;

import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DifferentIndividualsAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointDataPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DisjointObjectPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EquivalentDataPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.EquivalentObjectPropertiesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.InverseObjectPropertyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ObjectPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureIndex;
import org.industrial.ontology.kernel.api.index.SameIndividualAxiomsIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLDataFactory;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link OwlOntologyFacade}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.OwlOntologyFacadeFactory} (generated in the legacy build).
 */
public final class OwlOntologyFacadeFactory {

    private final Supplier<OntologyAnnotationsIndex> ontologyAnnotationsIndex;

    private final Supplier<OntologySignatureIndex> ontologySignatureIndex;

    private final Supplier<OntologyAxiomsIndex> ontologyAxiomsIndex;

    private final Supplier<EntitiesInOntologySignatureByIriIndex> entitiesInOntologySignatureByIriIndex;

    private final Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionAxiomsBySubjectIndex;

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<SubAnnotationPropertyAxiomsBySubPropertyIndex> subAnnotationPropertyAxiomsBySubPropertyIndex;

    private final Supplier<AnnotationPropertyDomainAxiomsIndex> annotationPropertyDomainAxiomsIndex;

    private final Supplier<AnnotationPropertyRangeAxiomsIndex> annotationPropertyRangeAxiomsIndex;

    private final Supplier<SubClassOfAxiomsBySubClassIndex> subClassOfAxiomsBySubClassIndex;

    private final Supplier<AxiomsByReferenceIndex> axiomsByReferenceIndex;

    private final Supplier<EquivalentClassesAxiomsIndex> equivalentClassesAxiomsIndex;

    private final Supplier<DisjointClassesAxiomsIndex> disjointClassesAxiomsIndex;

    private final Supplier<AxiomsByTypeIndex> axiomsByTypeIndex;

    private final Supplier<SubObjectPropertyAxiomsBySubPropertyIndex> subObjectPropertyAxiomsBySubPropertyIndex;

    private final Supplier<ObjectPropertyDomainAxiomsIndex> objectPropertyDomainAxiomsIndex;

    private final Supplier<ObjectPropertyRangeAxiomsIndex> objectPropertyRangeAxiomsIndex;

    private final Supplier<InverseObjectPropertyAxiomsIndex> inverseObjectPropertyAxiomsIndex;

    private final Supplier<EquivalentObjectPropertiesAxiomsIndex> equivalentObjectPropertiesAxiomsIndex;

    private final Supplier<DisjointObjectPropertiesAxiomsIndex> disjointObjectPropertiesAxiomsIndex;

    private final Supplier<SubDataPropertyAxiomsBySubPropertyIndex> subDataPropertyAxiomsBySubPropertyIndex;

    private final Supplier<DataPropertyDomainAxiomsIndex> dataPropertyDomainAxiomsIndex;

    private final Supplier<DataPropertyRangeAxiomsIndex> dataPropertyRangeAxiomsIndex;

    private final Supplier<EquivalentDataPropertiesAxiomsIndex> equivalentDataPropertiesAxiomsIndex;

    private final Supplier<DisjointDataPropertiesAxiomsIndex> disjointDataPropertiesAxiomsIndex;

    private final Supplier<ClassAssertionAxiomsByIndividualIndex> classAssertionAxiomsByIndividualIndex;

    private final Supplier<ClassAssertionAxiomsByClassIndex> classAssertionAxiomsByClassIndex;

    private final Supplier<DataPropertyAssertionAxiomsBySubjectIndex> dataPropertyAssertionAxiomsBySubjectIndex;

    private final Supplier<ObjectPropertyAssertionAxiomsBySubjectIndex> objectPropertyAssertionAxiomsBySubjectIndex;

    private final Supplier<SameIndividualAxiomsIndex> sameIndividualAxiomsIndex;

    private final Supplier<DifferentIndividualsAxiomsIndex> differentIndividualsAxiomsIndex;

    public OwlOntologyFacadeFactory(Supplier<OntologyAnnotationsIndex> ontologyAnnotationsIndex,
            Supplier<OntologySignatureIndex> ontologySignatureIndex,
            Supplier<OntologyAxiomsIndex> ontologyAxiomsIndex,
            Supplier<EntitiesInOntologySignatureByIriIndex> entitiesInOntologySignatureByIriIndex,
            Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionAxiomsBySubjectIndex,
            Supplier<OWLDataFactory> dataFactory,
            Supplier<SubAnnotationPropertyAxiomsBySubPropertyIndex> subAnnotationPropertyAxiomsBySubPropertyIndex,
            Supplier<AnnotationPropertyDomainAxiomsIndex> annotationPropertyDomainAxiomsIndex,
            Supplier<AnnotationPropertyRangeAxiomsIndex> annotationPropertyRangeAxiomsIndex,
            Supplier<SubClassOfAxiomsBySubClassIndex> subClassOfAxiomsBySubClassIndex,
            Supplier<AxiomsByReferenceIndex> axiomsByReferenceIndex,
            Supplier<EquivalentClassesAxiomsIndex> equivalentClassesAxiomsIndex,
            Supplier<DisjointClassesAxiomsIndex> disjointClassesAxiomsIndex,
            Supplier<AxiomsByTypeIndex> axiomsByTypeIndex,
            Supplier<SubObjectPropertyAxiomsBySubPropertyIndex> subObjectPropertyAxiomsBySubPropertyIndex,
            Supplier<ObjectPropertyDomainAxiomsIndex> objectPropertyDomainAxiomsIndex,
            Supplier<ObjectPropertyRangeAxiomsIndex> objectPropertyRangeAxiomsIndex,
            Supplier<InverseObjectPropertyAxiomsIndex> inverseObjectPropertyAxiomsIndex,
            Supplier<EquivalentObjectPropertiesAxiomsIndex> equivalentObjectPropertiesAxiomsIndex,
            Supplier<DisjointObjectPropertiesAxiomsIndex> disjointObjectPropertiesAxiomsIndex,
            Supplier<SubDataPropertyAxiomsBySubPropertyIndex> subDataPropertyAxiomsBySubPropertyIndex,
            Supplier<DataPropertyDomainAxiomsIndex> dataPropertyDomainAxiomsIndex,
            Supplier<DataPropertyRangeAxiomsIndex> dataPropertyRangeAxiomsIndex,
            Supplier<EquivalentDataPropertiesAxiomsIndex> equivalentDataPropertiesAxiomsIndex,
            Supplier<DisjointDataPropertiesAxiomsIndex> disjointDataPropertiesAxiomsIndex,
            Supplier<ClassAssertionAxiomsByIndividualIndex> classAssertionAxiomsByIndividualIndex,
            Supplier<ClassAssertionAxiomsByClassIndex> classAssertionAxiomsByClassIndex,
            Supplier<DataPropertyAssertionAxiomsBySubjectIndex> dataPropertyAssertionAxiomsBySubjectIndex,
            Supplier<ObjectPropertyAssertionAxiomsBySubjectIndex> objectPropertyAssertionAxiomsBySubjectIndex,
            Supplier<SameIndividualAxiomsIndex> sameIndividualAxiomsIndex,
            Supplier<DifferentIndividualsAxiomsIndex> differentIndividualsAxiomsIndex) {
        this.ontologyAnnotationsIndex = java.util.Objects.requireNonNull(ontologyAnnotationsIndex);
        this.ontologySignatureIndex = java.util.Objects.requireNonNull(ontologySignatureIndex);
        this.ontologyAxiomsIndex = java.util.Objects.requireNonNull(ontologyAxiomsIndex);
        this.entitiesInOntologySignatureByIriIndex = java.util.Objects.requireNonNull(entitiesInOntologySignatureByIriIndex);
        this.annotationAssertionAxiomsBySubjectIndex = java.util.Objects.requireNonNull(annotationAssertionAxiomsBySubjectIndex);
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.subAnnotationPropertyAxiomsBySubPropertyIndex = java.util.Objects.requireNonNull(subAnnotationPropertyAxiomsBySubPropertyIndex);
        this.annotationPropertyDomainAxiomsIndex = java.util.Objects.requireNonNull(annotationPropertyDomainAxiomsIndex);
        this.annotationPropertyRangeAxiomsIndex = java.util.Objects.requireNonNull(annotationPropertyRangeAxiomsIndex);
        this.subClassOfAxiomsBySubClassIndex = java.util.Objects.requireNonNull(subClassOfAxiomsBySubClassIndex);
        this.axiomsByReferenceIndex = java.util.Objects.requireNonNull(axiomsByReferenceIndex);
        this.equivalentClassesAxiomsIndex = java.util.Objects.requireNonNull(equivalentClassesAxiomsIndex);
        this.disjointClassesAxiomsIndex = java.util.Objects.requireNonNull(disjointClassesAxiomsIndex);
        this.axiomsByTypeIndex = java.util.Objects.requireNonNull(axiomsByTypeIndex);
        this.subObjectPropertyAxiomsBySubPropertyIndex = java.util.Objects.requireNonNull(subObjectPropertyAxiomsBySubPropertyIndex);
        this.objectPropertyDomainAxiomsIndex = java.util.Objects.requireNonNull(objectPropertyDomainAxiomsIndex);
        this.objectPropertyRangeAxiomsIndex = java.util.Objects.requireNonNull(objectPropertyRangeAxiomsIndex);
        this.inverseObjectPropertyAxiomsIndex = java.util.Objects.requireNonNull(inverseObjectPropertyAxiomsIndex);
        this.equivalentObjectPropertiesAxiomsIndex = java.util.Objects.requireNonNull(equivalentObjectPropertiesAxiomsIndex);
        this.disjointObjectPropertiesAxiomsIndex = java.util.Objects.requireNonNull(disjointObjectPropertiesAxiomsIndex);
        this.subDataPropertyAxiomsBySubPropertyIndex = java.util.Objects.requireNonNull(subDataPropertyAxiomsBySubPropertyIndex);
        this.dataPropertyDomainAxiomsIndex = java.util.Objects.requireNonNull(dataPropertyDomainAxiomsIndex);
        this.dataPropertyRangeAxiomsIndex = java.util.Objects.requireNonNull(dataPropertyRangeAxiomsIndex);
        this.equivalentDataPropertiesAxiomsIndex = java.util.Objects.requireNonNull(equivalentDataPropertiesAxiomsIndex);
        this.disjointDataPropertiesAxiomsIndex = java.util.Objects.requireNonNull(disjointDataPropertiesAxiomsIndex);
        this.classAssertionAxiomsByIndividualIndex = java.util.Objects.requireNonNull(classAssertionAxiomsByIndividualIndex);
        this.classAssertionAxiomsByClassIndex = java.util.Objects.requireNonNull(classAssertionAxiomsByClassIndex);
        this.dataPropertyAssertionAxiomsBySubjectIndex = java.util.Objects.requireNonNull(dataPropertyAssertionAxiomsBySubjectIndex);
        this.objectPropertyAssertionAxiomsBySubjectIndex = java.util.Objects.requireNonNull(objectPropertyAssertionAxiomsBySubjectIndex);
        this.sameIndividualAxiomsIndex = java.util.Objects.requireNonNull(sameIndividualAxiomsIndex);
        this.differentIndividualsAxiomsIndex = java.util.Objects.requireNonNull(differentIndividualsAxiomsIndex);
    }

    public OwlOntologyFacade create(@Nonnull OWLOntologyID ontologyID) {
        return new OwlOntologyFacade(ontologyID, ontologyAnnotationsIndex.get(), ontologySignatureIndex.get(), ontologyAxiomsIndex.get(), entitiesInOntologySignatureByIriIndex.get(), annotationAssertionAxiomsBySubjectIndex.get(), dataFactory.get(), subAnnotationPropertyAxiomsBySubPropertyIndex.get(), annotationPropertyDomainAxiomsIndex.get(), annotationPropertyRangeAxiomsIndex.get(), subClassOfAxiomsBySubClassIndex.get(), axiomsByReferenceIndex.get(), equivalentClassesAxiomsIndex.get(), disjointClassesAxiomsIndex.get(), axiomsByTypeIndex.get(), subObjectPropertyAxiomsBySubPropertyIndex.get(), objectPropertyDomainAxiomsIndex.get(), objectPropertyRangeAxiomsIndex.get(), inverseObjectPropertyAxiomsIndex.get(), equivalentObjectPropertiesAxiomsIndex.get(), disjointObjectPropertiesAxiomsIndex.get(), subDataPropertyAxiomsBySubPropertyIndex.get(), dataPropertyDomainAxiomsIndex.get(), dataPropertyRangeAxiomsIndex.get(), equivalentDataPropertiesAxiomsIndex.get(), disjointDataPropertiesAxiomsIndex.get(), classAssertionAxiomsByIndividualIndex.get(), classAssertionAxiomsByClassIndex.get(), dataPropertyAssertionAxiomsBySubjectIndex.get(), objectPropertyAssertionAxiomsBySubjectIndex.get(), sameIndividualAxiomsIndex.get(), differentIndividualsAxiomsIndex.get());
    }
}
