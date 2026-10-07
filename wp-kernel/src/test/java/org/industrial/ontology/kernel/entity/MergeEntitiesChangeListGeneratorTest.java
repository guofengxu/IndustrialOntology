package org.industrial.ontology.kernel.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManagerImpl;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.SKOSVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import static org.industrial.ontology.domain.entity.MergedEntityTreatment.DELETE_MERGED_ENTITY;
import static org.industrial.ontology.domain.entity.MergedEntityTreatment.DEPRECATE_MERGED_ENTITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import org.industrial.ontology.kernel.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.index.AnnotationAxiomsByIriReferenceIndex;
import org.industrial.ontology.kernel.index.AxiomsByEntityReferenceIndex;
import org.industrial.ontology.kernel.index.AxiomsByReferenceIndex;
import org.industrial.ontology.kernel.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.index.ProjectOntologiesIndex;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.AnnotationAssertion;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.IRI;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Literal;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.SubClassOf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.entity.MergeEntitiesChangeListGenerator_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Mar 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MergeEntitiesChangeListGeneratorTest {

    @Mock
    private ProjectId projectId;

    @Mock
    private EntityDiscussionThreadRepository discussionThreadRepo;

    private OWLDataFactory dataFactory;

    private ImmutableSet<OWLEntity> sourceEntities;

    @Mock
    private OWLOntologyID ontologyId;

    private OWLClass targetEntity;

    private OWLAnnotationProperty rdfsLabel;

    private OWLAnnotationProperty skosPrefLabel;

    private OWLAnnotationProperty skosAltLabel;

    private OWLAnnotationProperty rdfsComment;

    private OWLAnnotationValue hello;

    private OWLAnnotationValue bonjour;

    private OWLAnnotationValue hi;

    private OWLClass clsC;

    private OWLClass sourceEntity;

    private EntityRenamer entityRenamer;

    private DefaultOntologyIdManagerImpl defaultOntologyIdManager;

    private ProjectOntologiesIndex projectOntologiesIndex;

    private AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    private AxiomsByEntityReferenceIndex axiomsByEntityReference;

    private AxiomsByReferenceIndex axiomsByReferenceIndex;

    private OntologyAxiomsIndex ontologyAxiomsIndex;

    private AnnotationAxiomsByIriReferenceIndex axiomsByIriReference;

    private AxiomsByTypeIndex axiomsByTypeIndex;

    @BeforeEach
    public void setUp() throws Exception {
        dataFactory = new OWLDataFactoryImpl();
        IRI iriA = IRI.create("http://ontology.org/A");
        IRI iriB = IRI.create("http://ontology.org/B");
        IRI iriC = IRI.create("http://ontology.org/C");
        sourceEntity = Class(iriA);
        sourceEntities = ImmutableSet.of(sourceEntity);
        targetEntity = Class(iriB);
        clsC = Class(iriC);
        rdfsLabel = dataFactory.getRDFSLabel();
        skosPrefLabel = dataFactory.getOWLAnnotationProperty(SKOSVocabulary.PREFLABEL.getIRI());
        skosAltLabel = dataFactory.getOWLAnnotationProperty(SKOSVocabulary.ALTLABEL.getIRI());
        rdfsComment = dataFactory.getRDFSComment();
        hello = Literal("Hello", "en");
        bonjour = Literal("Bonjour", "fr");
        hi = Literal("hi", "en");
        var ontologyChanges = ImmutableList.<OntologyChange>of(AddAxiomChange.of(ontologyId, SubClassOf(sourceEntity, clsC)), AddAxiomChange.of(ontologyId, SubClassOf(targetEntity, clsC)), AddAxiomChange.of(ontologyId, AnnotationAssertion(rdfsLabel, sourceEntity.getIRI(), hello)), AddAxiomChange.of(ontologyId, AnnotationAssertion(skosPrefLabel, sourceEntity.getIRI(), bonjour)), AddAxiomChange.of(ontologyId, AnnotationAssertion(rdfsComment, sourceEntity.getIRI(), hi)));
        projectOntologiesIndex = new ProjectOntologiesIndex();
        defaultOntologyIdManager = new DefaultOntologyIdManagerImpl(projectOntologiesIndex);
        annotationAssertionsIndex = new AnnotationAssertionAxiomsBySubjectIndex();
        axiomsByEntityReference = new AxiomsByEntityReferenceIndex(dataFactory);
        axiomsByIriReference = new AnnotationAxiomsByIriReferenceIndex();
        axiomsByReferenceIndex = new AxiomsByReferenceIndex(axiomsByEntityReference, axiomsByIriReference);
        axiomsByTypeIndex = new AxiomsByTypeIndex();
        ontologyAxiomsIndex = new OntologyAxiomsIndex(axiomsByTypeIndex);
        entityRenamer = new EntityRenamer(dataFactory, this.projectOntologiesIndex, axiomsByReferenceIndex);
        applyChanges(ontologyChanges);
    }

    private void applyChanges(ImmutableList<OntologyChange> changes) {
        projectOntologiesIndex.applyChanges(changes);
        axiomsByEntityReference.applyChanges(changes);
        axiomsByIriReference.applyChanges(changes);
        axiomsByTypeIndex.applyChanges(changes);
        annotationAssertionsIndex.applyChanges(changes);
    }

    private void createGeneratorAndApplyChanges(MergedEntityTreatment treatment) {
        MergeEntitiesChangeListGenerator gen = new MergeEntitiesChangeListGenerator(sourceEntities, targetEntity, treatment, "The commit message", projectId, dataFactory, discussionThreadRepo, entityRenamer, defaultOntologyIdManager, projectOntologiesIndex, annotationAssertionsIndex);
        OntologyChangeList<?> changeList = gen.generateChanges(new ChangeGenerationContext(UserId.getUserId("Bob")));
        applyChanges(ImmutableList.copyOf(changeList.getChanges()));
    }

    @Test
    public void shouldReplaceSourceWithTarget_WithDeleteTreatment() {
        createGeneratorAndApplyChanges(DELETE_MERGED_ENTITY);
        assertThat(axiomsByEntityReference.containsEntityInOntologyAxiomsSignature(sourceEntity, ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(SubClassOf(sourceEntity, clsC), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(SubClassOf(targetEntity, clsC), ontologyId), is(true));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(rdfsComment, sourceEntity.getIRI(), hi), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(rdfsComment, targetEntity.getIRI(), hi), ontologyId), is(true));
    }

    @Test
    public void shouldReplaceSourceRdfsLabelWithTargetAltLabel_WithDeleteTreatment() {
        createGeneratorAndApplyChanges(DELETE_MERGED_ENTITY);
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(rdfsLabel, sourceEntity.getIRI(), hello), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(rdfsLabel, targetEntity.getIRI(), hello), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(skosAltLabel, targetEntity.getIRI(), hello), ontologyId), is(true));
    }

    @Test
    public void shouldReplaceSourceSkosPrefLabelWithTargetAltLabel_WithDeleteTreatment() {
        createGeneratorAndApplyChanges(DELETE_MERGED_ENTITY);
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(skosPrefLabel, sourceEntity.getIRI(), bonjour), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(skosPrefLabel, targetEntity.getIRI(), bonjour), ontologyId), is(false));
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(skosAltLabel, targetEntity.getIRI(), bonjour), ontologyId), is(true));
    }

    @Test
    public void shouldNotAddDeprecationWith_WithDeleteTreatment() {
        createGeneratorAndApplyChanges(DELETE_MERGED_ENTITY);
        OWLAnnotationProperty deprecated = dataFactory.getOWLDeprecated();
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(deprecated, sourceEntity.getIRI(), Literal(true)), ontologyId), is(false));
    }

    @Test
    public void shouldAddDeprecationWith_WithDeprecateTreatment() {
        createGeneratorAndApplyChanges(DEPRECATE_MERGED_ENTITY);
        OWLAnnotationProperty deprecated = dataFactory.getOWLDeprecated();
        assertThat(ontologyAxiomsIndex.containsAxiom(AnnotationAssertion(deprecated, sourceEntity.getIRI(), Literal(true)), ontologyId), is(true));
    }
}
