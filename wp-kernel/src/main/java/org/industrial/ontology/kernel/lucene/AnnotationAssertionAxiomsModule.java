package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsByValueIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectAnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.AnnotationAssertionAxiomsModule}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-13
 */
public class AnnotationAssertionAxiomsModule {

    private ProjectOntologiesIndex projectOntologiesIndex;

    private ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubjectIndex;

    private ProjectSignatureIndex provideProjectSignatureIndex;

    private EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex;

    private EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex;

    private AnnotationAssertionAxiomsByValueIndex annotationAssertionAxiomsByValueIndex;

    public AnnotationAssertionAxiomsModule(ProjectOntologiesIndex projectOntologiesIndex,
                                           ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubjectIndex,
                                           ProjectSignatureIndex provideProjectSignatureIndex,
                                           EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex,
                                           EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex,
                                           AnnotationAssertionAxiomsByValueIndex annotationAssertionAxiomsByValueIndex) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.annotationAssertionAxiomsBySubjectIndex = annotationAssertionAxiomsBySubjectIndex;
        this.provideProjectSignatureIndex = provideProjectSignatureIndex;
        this.entitiesInProjectSignatureIndex = entitiesInProjectSignatureIndex;
        this.entitiesInProjectSignatureByIriIndex = entitiesInProjectSignatureByIriIndex;
        this.annotationAssertionAxiomsByValueIndex = annotationAssertionAxiomsByValueIndex;
    }

    ProjectAnnotationAssertionAxiomsBySubjectIndex provideAnnotationAssertionAxiomsBySubjectIndex() {
        return annotationAssertionAxiomsBySubjectIndex;
    }

    public ProjectSignatureIndex getProvideProjectSignatureIndex() {
        return provideProjectSignatureIndex;
    }

    public EntitiesInProjectSignatureIndex provideEntitiesInProjectSignatureIndex() {
        return entitiesInProjectSignatureIndex;
    }

    public AnnotationAssertionAxiomsByValueIndex provideAnnotationAssertionAxiomsByValueIndex() {
        return annotationAssertionAxiomsByValueIndex;
    }

    public EntitiesInProjectSignatureByIriIndex provideEntitiesInProjectSignatureByIriIndex() {
        return entitiesInProjectSignatureByIriIndex;
    }

    public ProjectOntologiesIndex provideProjectOntologiesIndex() {
        return projectOntologiesIndex;
    }
}
